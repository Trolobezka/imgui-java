package imgui;

import imgui.assertion.ImAssertCallback;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiDragDropFlags;
import imgui.flag.ImGuiInputTextFlags;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.ref.WeakReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class ImGui {
    private static final String LIB_PATH_PROP = "imgui.library.path";
    private static final String LIB_NAME_PROP = "imgui.library.name";
    private static final String LIB_NAME_DEFAULT = System.getProperty("os.arch").contains("64") ? "imgui-java64" : "imgui-java";
    private static final String LIB_TMP_DIR_PREFIX = "imgui-java-natives_" + System.currentTimeMillis();

    private static final ImDrawList BACKGROUND_DRAW_LIST;
    private static final ImDrawList FOREGROUND_DRAW_LIST;
    private static final ImGuiStorage IMGUI_STORAGE;
    private static final ImGuiViewport FIND_VIEWPORT;

    private static final ImGuiViewport MAIN_VIEWPORT;
    private static final ImGuiPlatformIO PLATFORM_IO;

    static {
        final String libPath = System.getProperty(LIB_PATH_PROP);
        final String libName = System.getProperty(LIB_NAME_PROP, LIB_NAME_DEFAULT);
        final String fullLibName = resolveFullLibName();

        final String extractedLibAbsPath = tryLoadFromClasspath(fullLibName);

        if (libPath != null) {
            System.load(Paths.get(libPath).resolve(fullLibName).toFile().getAbsolutePath());
        } else if (extractedLibAbsPath != null) {
            System.load(extractedLibAbsPath);
        } else {
            System.loadLibrary(libName);
        }

        BACKGROUND_DRAW_LIST = new ImDrawList(0);
        FOREGROUND_DRAW_LIST = new ImDrawList(0);
        IMGUI_STORAGE = new ImGuiStorage(0);
        FIND_VIEWPORT = new ImGuiViewport(0);

        MAIN_VIEWPORT = new ImGuiViewport(0);
        PLATFORM_IO = new ImGuiPlatformIO(0);

        nInitJni();
        ImFontAtlas.nInit();
        ImGuiPlatformIO.init();
        nInitInputTextData();

        setAssertCallback(new ImAssertCallback() {
            @Override
            public void imAssertCallback(String assertion, int line, String file) {
                System.err.println("Dear ImGui Assertion Failed: " + assertion);
                System.err.println("Assertion Located At: " + file + ":" + line);
                Thread.dumpStack();
            }
        });
    }

    private static String resolveFullLibName() {
        final boolean isWin = System.getProperty("os.name").toLowerCase().contains("win");
        final boolean isMac = System.getProperty("os.name").toLowerCase().contains("mac");
        final String libPrefix;
        final String libSuffix;

        if (isWin) {
            libPrefix = "";
            libSuffix = ".dll";
        } else if (isMac) {
            libPrefix = "lib";
            libSuffix = ".dylib";
        } else {
            libPrefix = "lib";
            libSuffix = ".so";
        }

        return System.getProperty(LIB_NAME_PROP, libPrefix + LIB_NAME_DEFAULT + libSuffix);
    }

    // This method tries to unpack the library binary from classpath into the temp dir.
    private static String tryLoadFromClasspath(final String fullLibName) {
        try (InputStream is = ImGui.class.getClassLoader().getResourceAsStream("io/imgui/java/native-bin/" + fullLibName)) {
            if (is == null) {
                return null;
            }

            final Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve(LIB_TMP_DIR_PREFIX);
            tmpDir.toFile().mkdirs();

            final Path libBin = tmpDir.resolve(fullLibName);
            Files.copy(is, libBin, StandardCopyOption.REPLACE_EXISTING);
            libBin.toFile().deleteOnExit();

            return libBin.toFile().getAbsolutePath();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * For internal usage.
     * Method is used to initiate static instantiation (loading of the native libraries etc.).
     * Otherwise native libraries will be loaded on demand and natively mapped objects won't work.
     */
    public static void init() {
    }

    /*JNI
        #include "_common.h"
     */

    private static native void nInitJni(); /*
        Jni::InitJvm(env);
        Jni::InitCommon(env);
        Jni::InitAssertion(env);
        Jni::InitCallbacks(env);
        Jni::InitBindingStruct(env);
    */

    /**
     * Set a custom assertion callback for ImGui assertions.
     * Take note: Any custom assertion callback SHOULD NOT throw any exception.
     * After any callback the application will be terminated, any attempt to bypass this behavior
     * will result in a EXCEPTION_ACCESS_VIOLATION from within the native layer.
     *
     * @param callback The custom ImGui assertion callback
     */
    public static native void setAssertCallback(ImAssertCallback callback); /*
        Jni::SetAssertionCallback(callback);
    */

    // CUSTOM API: BEGIN
    public static native void pushStyleColor(int imGuiCol, int r, int g, int b, int a); /*
        ImGui::PushStyleColor(imGuiCol, (ImU32)ImColor((int)r, (int)g, (int)b, (int)a));
    */

    public static native int getColorU32i(int col); /*
        return ImGui::GetColorU32((ImU32)col);
    */

    public static native boolean checkbox(String label, boolean active); /*
        bool flag = (bool)active;
        return ImGui::Checkbox(label, &flag);
    */

    public static boolean combo(final String label, final ImInt currentItem, final String[] items) {
        return nCombo(label, currentItem.getData(), items, items.length, -1);
    }

    public static boolean combo(final String label, final ImInt currentItem, final String[] items, final int popupMaxHeightInItems) {
        return nCombo(label, currentItem.getData(), items, items.length, popupMaxHeightInItems);
    }

    private static native boolean nCombo(String label, int[] currentItem, String[] items, int itemsCount, int popupMaxHeightInItems); /*
        const char* listboxItems[itemsCount];

        for (int i = 0; i < itemsCount; i++) {
            jstring string = (jstring)env->GetObjectArrayElement(items, i);
            const char* rawString = env->GetStringUTFChars(string, JNI_FALSE);
            listboxItems[i] = rawString;
        }

        bool flag = ImGui::Combo(label, &currentItem[0], listboxItems, itemsCount, popupMaxHeightInItems);

        for (int i = 0; i< itemsCount; i++) {
            jstring string = (jstring)env->GetObjectArrayElement(items, i);
            env->ReleaseStringUTFChars(string, listboxItems[i]);
        }

        return flag;
    */

    // Widgets: Input with Keyboard
    // - If you want to use InputText() with std::string or any custom dynamic string type, see misc/cpp/imgui_stdlib.h and comments in imgui_demo.cpp.
    // - Most of the ImGuiInputTextFlags flags are only useful for InputText() and not for InputFloatX, InputIntX, InputDouble etc.

    /*JNI
        jmethodID jImStringResizeInternalMID;

        jfieldID inputDataSizeID;
        jfieldID inputDataIsDirtyID;
        jfieldID inputDataIsResizedID;

        struct InputTextCallbackUserData {
            JNIEnv* env;
            jobject* imString;
            int maxSize;
            jbyteArray jResizedBuf;
            char* resizedBuf;
            jobject* textInputData;
            char* allowedChars;
        };

        static int TextEditCallbackStub(ImGuiInputTextCallbackData* data) {
            InputTextCallbackUserData* userData = (InputTextCallbackUserData*)data->UserData;

            if (data->EventFlag == ImGuiInputTextFlags_CallbackCharFilter) {
                int allowedCharLength = strlen(userData->allowedChars);
                if(allowedCharLength > 0) {
                    bool found = false;
                    for(int i = 0; i < allowedCharLength; i++) {
                        if(userData->allowedChars[i] == data->EventChar) {
                            found = true;
                            break;
                        }
                    }
                    return found ? 0 : 1;
                }
            } else if (data->EventFlag == ImGuiInputTextFlags_CallbackResize) {
                int newSize = data->BufTextLen;
                if (newSize >= userData->maxSize) {
                    JNIEnv* env = userData->env;

                    jbyteArray newBufArr = (jbyteArray)env->CallObjectMethod(*userData->imString, jImStringResizeInternalMID, newSize);
                    char* newBuf = (char*)env->GetPrimitiveArrayCritical(newBufArr, 0);

                    data->Buf = newBuf;

                    userData->jResizedBuf = newBufArr;
                    userData->resizedBuf = newBuf;
                }
            }

            return 0;
        }
    */

    private static native void nInitInputTextData(); /*
        jclass jInputDataClass = env->FindClass("imgui/type/ImString$InputData");
        inputDataSizeID = env->GetFieldID(jInputDataClass, "size", "I");
        inputDataIsDirtyID = env->GetFieldID(jInputDataClass, "isDirty", "Z");
        inputDataIsResizedID = env->GetFieldID(jInputDataClass, "isResized", "Z");

        jclass jImString = env->FindClass("imgui/type/ImString");
        jImStringResizeInternalMID = env->GetMethodID(jImString, "resizeInternal", "(I)[B");
    */

    public static boolean inputText(String label, ImString text) {
        return preInputText(false, label, null, text, 0, 0, ImGuiInputTextFlags.None);
    }

    public static boolean inputText(String label, ImString text, int imGuiInputTextFlags) {
        return preInputText(false, label, null, text, 0, 0, imGuiInputTextFlags);
    }

    public static boolean inputTextMultiline(String label, ImString text) {
        return preInputText(true, label, null, text, 0, 0, ImGuiInputTextFlags.None);
    }

    public static boolean inputTextMultiline(String label, ImString text, float width, float height) {
        return preInputText(true, label, null, text, width, height, ImGuiInputTextFlags.None);
    }

    public static boolean inputTextMultiline(String label, ImString text, int imGuiInputTextFlags) {
        return preInputText(true, label, null, text, 0, 0, imGuiInputTextFlags);
    }

    public static boolean inputTextMultiline(String label, ImString text, float width, float height, int imGuiInputTextFlags) {
        return preInputText(true, label, null, text, width, height, imGuiInputTextFlags);
    }

    public static boolean inputTextWithHint(String label, String hint, ImString text) {
        return preInputText(false, label, hint, text, 0, 0, ImGuiInputTextFlags.None);
    }

    public static boolean inputTextWithHint(String label, String hint, ImString text, int imGuiInputTextFlags) {
        return preInputText(false, label, hint, text, 0, 0, imGuiInputTextFlags);
    }

    private static boolean preInputText(boolean multiline, String label, String hint, ImString text, float width, float height, int flags) {
        final ImString.InputData inputData = text.inputData;

        if (inputData.isResizable) {
            flags |= ImGuiInputTextFlags.CallbackResize;
        }

        if (!inputData.allowedChars.isEmpty()) {
            flags |= ImGuiInputTextFlags.CallbackCharFilter;
        }

        String hintLabel = hint;
        if (hintLabel == null) {
            hintLabel = "";
        }

        return nInputText(multiline, hint != null, label, hintLabel, text, text.getData(), text.getData().length, width, height, flags, inputData, inputData.allowedChars);
    }

    private static native boolean nInputText(boolean multiline, boolean hint, String label, String hintLabel, ImString imString, byte[] buf, int maxSize, float width, float height, int flags, ImString.InputData textInputData, String allowedChars); /*
        InputTextCallbackUserData userData;
        userData.imString = &imString;
        userData.maxSize = maxSize;
        userData.jResizedBuf = NULL;
        userData.resizedBuf = NULL;
        userData.textInputData = &textInputData;
        userData.env = env;
        userData.allowedChars = allowedChars;

        bool valueChanged;

        if (multiline) {
            valueChanged = ImGui::InputTextMultiline(label, buf, maxSize, ImVec2(width, height), flags, &TextEditCallbackStub, &userData);
        } else if (hint) {
            valueChanged = ImGui::InputTextWithHint(label, hintLabel, buf, maxSize, flags, &TextEditCallbackStub, &userData);
        } else {
            valueChanged = ImGui::InputText(label, buf, maxSize, flags, &TextEditCallbackStub, &userData);
        }

        if (valueChanged) {
            int size;

            if (userData.jResizedBuf != NULL) {
                size = strlen(userData.resizedBuf);
                env->ReleasePrimitiveArrayCritical(userData.jResizedBuf, userData.resizedBuf, 0);
            } else {
                size = strlen(buf);
            }

            env->SetIntField(textInputData, inputDataSizeID, size);
            env->SetBooleanField(textInputData, inputDataIsDirtyID, true);
        }

        return valueChanged;
    */

    public static void listBox(String label, ImInt currentItem, String[] items) {
        nListBox(label, currentItem.getData(), items, items.length, -1);
    }

    public static void listBox(String label, ImInt currentItem, String[] items, int heightInItems) {
        nListBox(label, currentItem.getData(), items, items.length, heightInItems);
    }

    private static native boolean nListBox(String label, int[] currentItem, String[] items, int itemsCount, int heightInItems); /*
        const char* listboxItems[itemsCount];

        for (int i = 0; i < itemsCount; i++) {
            jstring string = (jstring)env->GetObjectArrayElement(items, i);
            const char* rawString = env->GetStringUTFChars(string, JNI_FALSE);
            listboxItems[i] = rawString;
        }

        bool flag = ImGui::ListBox(label, &currentItem[0], listboxItems, itemsCount, heightInItems);

        for (int i = 0; i< itemsCount; i++) {
            jstring string = (jstring)env->GetObjectArrayElement(items, i);
            env->ReleaseStringUTFChars(string, listboxItems[i]);
        }

        return flag;
    */

    // Widgets: Value() Helpers.
    // - Those are merely shortcut to calling Text() with a format string. Output single value in "name: value" format (tip: freely declare more in your code to handle your types. you can add functions to the ImGui namespace)

    public static native void value(String prefix, boolean b); /*
        ImGui::Value(prefix, b);
    */

    public static native void value(String prefix, int v); /*
        ImGui::Value(prefix, (int)v);
    */

    public static native void value(String prefix, long v); /*
        ImGui::Value(prefix, (unsigned int)v);
    */

    public static native void value(String prefix, float f); /*
        ImGui::Value(prefix, f);
    */

    public static native void value(String prefix, float f, String floatFormat); /*
        ImGui::Value(prefix, f, floatFormat);
    */

    // Widgets: Menus
    // - Use BeginMenuBar() on a window ImGuiWindowFlags_MenuBar to append to its menu bar.
    // - Use BeginMainMenuBar() to create a menu bar at the top of the screen and append to it.
    // - Use BeginMenu() to create a menu. You can call BeginMenu() multiple time with the same identifier to append more items to it.

    /**
     * Append to menu-bar of current window (requires ImGuiWindowFlags_MenuBar flag set on parent window).
     */
    public static native boolean beginMenuBar(); /*
        return ImGui::BeginMenuBar();
    */

    /**
     * Only call EndMenuBar() if BeginMenuBar() returns true!
     */
    public static native void endMenuBar(); /*
        ImGui::EndMenuBar();
    */

    /**
     * Create and append to a full screen menu-bar.
     */
    public static native boolean beginMainMenuBar(); /*
        return ImGui::BeginMainMenuBar();
    */

    /**
     * Only call EndMainMenuBar() if BeginMainMenuBar() returns true!
     */
    public static native void endMainMenuBar(); /*
        ImGui::EndMainMenuBar();
    */

    /**
     * Create a sub-menu entry. only call EndMenu() if this returns true!
     */
    public static native boolean beginMenu(String label); /*
        return ImGui::BeginMenu(label);
    */

    /**
     * Create a sub-menu entry. only call EndMenu() if this returns true!
     */
    public static native boolean beginMenu(String label, boolean enabled); /*
        return ImGui::BeginMenu(label, enabled);
    */

    /**
     * Only call EndMenu() if BeginMenu() returns true!
     */
    public static native void endMenu(); /*
        ImGui::EndMenu();
    */

    /**
     * Return true when activated. shortcuts are displayed for convenience but not processed by ImGui at the moment
     */
    public static native boolean menuItem(String label); /*
        return ImGui::MenuItem(label);
    */

    /**
     * Return true when activated. shortcuts are displayed for convenience but not processed by ImGui at the moment
     */
    public static boolean menuItem(String label, String shortcut) {
        return nMenuItem(label, shortcut, false, true);
    }

    /**
     * Return true when activated. shortcuts are displayed for convenience but not processed by ImGui at the moment
     */
    public static boolean menuItem(String label, String shortcut, boolean selected) {
        return nMenuItem(label, shortcut, selected, true);
    }

    /**
     * Return true when activated. shortcuts are displayed for convenience but not processed by ImGui at the moment
     */
    public static boolean menuItem(String label, String shortcut, boolean selected, boolean enabled) {
        return nMenuItem(label, shortcut, selected, enabled);
    }

    /**
     * Return true when activated + toggle (*pSelected) if pSelected != NULL
     */
    public static boolean menuItem(String label, String shortcut, ImBoolean pSelected) {
        return nMenuItem(label, shortcut, pSelected.getData(), true);
    }

    /**
     * Return true when activated + toggle (*pSelected) if pSelected != NULL
     */
    public static boolean menuItem(String label, String shortcut, ImBoolean pSelected, boolean enabled) {
        return nMenuItem(label, shortcut, pSelected.getData(), enabled);
    }

    /**
     * Return true when activated
     */
    private static native boolean nMenuItem(String labelObj, String shortcutObj, boolean selected, boolean enabled); /*MANUAL
        char* label = (char*)env->GetStringUTFChars(labelObj, JNI_FALSE);
        char* shortcut = NULL;
        if (shortcutObj != NULL)
            shortcut = (char*)env->GetStringUTFChars(shortcutObj, JNI_FALSE);

        jboolean result = ImGui::MenuItem(label, shortcut, selected, enabled);

        if (shortcutObj != NULL)
            env->ReleaseStringUTFChars(shortcutObj, shortcut);
        env->ReleaseStringUTFChars(labelObj, label);

        return result;
    */

    /**
     * Return true when activated + toggle (*pSelected) if pSelected != NULL
     */
    private static native boolean nMenuItem(String labelObj, String shortcutObj, boolean[] pSelectedObj, boolean enabled); /*MANUAL
        char* label = (char*)env->GetStringUTFChars(labelObj, JNI_FALSE);
        char* shortcut = NULL;
        if (shortcutObj != NULL)
            shortcut = (char*)env->GetStringUTFChars(shortcutObj, JNI_FALSE);
        bool* pSelected = (bool*)env->GetPrimitiveArrayCritical(pSelectedObj, JNI_FALSE);

        jboolean result = ImGui::MenuItem(label, shortcut, &pSelected[0], enabled);

        env->ReleasePrimitiveArrayCritical(pSelectedObj, pSelected, 0);
        if (shortcutObj != NULL)
            env->ReleaseStringUTFChars(shortcutObj, shortcut);
        env->ReleaseStringUTFChars(labelObj, label);

        return result;
    */

    // Tooltips
    // - Tooltip are windows following the mouse. They do not take focus away.

    /**
     * Begin/append a tooltip window. to create full-featured tooltip (with any kind of items).
     */
    public static native void beginTooltip(); /*
        ImGui::BeginTooltip();
    */

    public static native void endTooltip(); /*
        ImGui::EndTooltip();
    */

    /**
     * Set a text-only tooltip, typically use with ImGui::IsItemHovered(). override any previous call to SetTooltip().
     */
    public static native void setTooltip(String text); /*
        ImGui::SetTooltip(text, NULL);
    */

    // Popups, Modals
    //  - They block normal mouse hovering detection (and therefore most mouse interactions) behind them.
    //  - If not modal: they can be closed by clicking anywhere outside them, or by pressing ESCAPE.
    //  - Their visibility state (~bool) is held internally instead of being held by the programmer as we are used to with regular Begin*() calls.
    //  - The 3 properties above are related: we need to retain popup visibility state in the library because popups may be closed as any time.
    //  - You can bypass the hovering restriction by using ImGuiHoveredFlags_AllowWhenBlockedByPopup when calling IsItemHovered() or IsWindowHovered().
    //  - IMPORTANT: Popup identifiers are relative to the current ID stack, so OpenPopup and BeginPopup generally needs to be at the same level of the stack.
    //    This is sometimes leading to confusing mistakes. May rework this in the future.
    // Popups: begin/end functions
    //  - BeginPopup(): query popup state, if open start appending into the window. Call EndPopup() afterwards. ImGuiWindowFlags are forwarded to the window.
    //  - BeginPopupModal(): block every interactions behind the window, cannot be closed by user, add a dimming background, has a title bar.

    /**
     * Return true if the popup is open, and you can start outputting to it.
     */
    public static native boolean beginPopup(String strId); /*
        return ImGui::BeginPopup(strId);
    */

    /**
     * Return true if the popup is open, and you can start outputting to it.
     */
    public static native boolean beginPopup(String strId, int imGuiWindowFlags); /*
        return ImGui::BeginPopup(strId, imGuiWindowFlags);
    */

    /**
     * Return true if the popup is open, and you can start outputting to it.
     */
    public static native boolean beginPopupModal(String name); /*
        return ImGui::BeginPopupModal(name);
    */

    /**
     * Return true if the popup is open, and you can start outputting to it.
     */
    public static boolean beginPopupModal(String name, ImBoolean pOpen) {
        return nBeginPopupModal(name, pOpen.getData(), 0);
    }

    /**
     * Return true if the popup is open, and you can start outputting to it.
     */
    public static boolean beginPopupModal(String name, int imGuiWindowFlags) {
        return nBeginPopupModal(name, imGuiWindowFlags);
    }

    /**
     * Return true if the popup is open, and you can start outputting to it.
     */
    public static boolean beginPopupModal(String name, ImBoolean pOpen, int imGuiWindowFlags) {
        return nBeginPopupModal(name, pOpen.getData(), imGuiWindowFlags);
    }

    private static native boolean nBeginPopupModal(String name, int imGuiWindowFlags); /*
        return ImGui::BeginPopupModal(name, NULL, imGuiWindowFlags);
    */

    private static native boolean nBeginPopupModal(String name, boolean[] pOpen, int imGuiWindowFlags); /*
        return ImGui::BeginPopupModal(name, &pOpen[0], imGuiWindowFlags);
    */

    /**
     * Only call EndPopup() if BeginPopupXXX() returns true!
     */
    public static native void endPopup(); /*
        ImGui::EndPopup();
    */

    // Popups: open/close functions
    //  - OpenPopup(): set popup state to open. ImGuiPopupFlags are available for opening options.
    //  - If not modal: they can be closed by clicking anywhere outside them, or by pressing ESCAPE.
    //  - CloseCurrentPopup(): use inside the BeginPopup()/EndPopup() scope to close manually.
    //  - CloseCurrentPopup() is called by default by Selectable()/MenuItem() when activated (FIXME: need some options).
    //  - Use ImGuiPopupFlags_NoOpenOverExistingPopup to avoid opening a popup if there's already one at the same level. This is equivalent to e.g. testing for !IsAnyPopupOpen() prior to OpenPopup().

    /**
     * Call to mark popup as open (don't call every frame!).
     */
    public static native void openPopup(String strId); /*
        ImGui::OpenPopup(strId);
    */

    /**
     * Call to mark popup as open (don't call every frame!).
     */
    public static native void openPopup(String strId, int imGuiPopupFlags); /*
        ImGui::OpenPopup(strId, imGuiPopupFlags);
    */

    /**
     * Helper to open popup when clicked on last item. return true when just opened. (note: actually triggers on the mouse _released_ event to be consistent with popup behaviors)
     */
    public static native void openPopupOnItemClick(); /*
        ImGui::OpenPopupOnItemClick();
    */

    /**
     * Helper to open popup when clicked on last item. return true when just opened. (note: actually triggers on the mouse _released_ event to be consistent with popup behaviors)
     */
    public static native void openPopupOnItemClick(String strId); /*
        ImGui::OpenPopupOnItemClick(strId);
    */

    /**
     * Helper to open popup when clicked on last item. return true when just opened. (note: actually triggers on the mouse _released_ event to be consistent with popup behaviors)
     */
    public static native void openPopupOnItemClick(int imGuiPopupFlags); /*
        ImGui::OpenPopupOnItemClick(NULL, imGuiPopupFlags);
    */

    /**
     * Helper to open popup when clicked on last item. return true when just opened. (note: actually triggers on the mouse _released_ event to be consistent with popup behaviors)
     */
    public static native void openPopupOnItemClick(String strId, int imGuiPopupFlags); /*
        ImGui::OpenPopupOnItemClick(strId, imGuiPopupFlags);
    */

    /**
     * Manually close the popup we have begin-ed into.
     */
    public static native void closeCurrentPopup(); /*
        ImGui::CloseCurrentPopup();
    */

    // Popups: open+begin combined functions helpers
    //  - Helpers to do OpenPopup+BeginPopup where the Open action is triggered by e.g. hovering an item and right-clicking.
    //  - They are convenient to easily create context menus, hence the name.
    //  - IMPORTANT: Notice that BeginPopupContextXXX takes ImGuiPopupFlags just like OpenPopup() and unlike BeginPopup(). For full consistency, we may add ImGuiWindowFlags to the BeginPopupContextXXX functions in the future.
    //  - IMPORTANT: we exceptionally default their flags to 1 (== ImGuiPopupFlags_MouseButtonRight) for backward compatibility with older API taking 'int mouse_button = 1' parameter, so if you add other flags remember to re-add the ImGuiPopupFlags_MouseButtonRight.

    /**
     * Open+begin popup when clicked on last item. if you can pass a NULL str_id only if the previous item had an id.
     * If you want to use that on a non-interactive item such as Text() you need to pass in an explicit ID here. read comments in .cpp!
     */
    public static native boolean beginPopupContextItem(); /*
        return ImGui::BeginPopupContextItem();
    */

    /**
     * Open+begin popup when clicked on last item. if you can pass a NULL str_id only if the previous item had an id.
     * If you want to use that on a non-interactive item such as Text() you need to pass in an explicit ID here. read comments in .cpp!
     */
    public static native boolean beginPopupContextItem(String strId); /*
        return ImGui::BeginPopupContextItem(strId);
    */

    /**
     * Open+begin popup when clicked on last item. if you can pass a NULL str_id only if the previous item had an id.
     * If you want to use that on a non-interactive item such as Text() you need to pass in an explicit ID here. read comments in .cpp!
     */
    public static native boolean beginPopupContextItem(int imGuiPopupFlags); /*
        return ImGui::BeginPopupContextItem(NULL, imGuiPopupFlags);
    */

    /**
     * Open+begin popup when clicked on last item. if you can pass a NULL str_id only if the previous item had an id.
     * If you want to use that on a non-interactive item such as Text() you need to pass in an explicit ID here. read comments in .cpp!
     */
    public static native boolean beginPopupContextItem(String strId, int imGuiPopupFlags); /*
        return ImGui::BeginPopupContextItem(strId, imGuiPopupFlags);
    */

    /**
     * Open+begin popup when clicked on current window.
     */
    public static native boolean beginPopupContextWindow(); /*
        return ImGui::BeginPopupContextWindow();
    */

    /**
     * Open+begin popup when clicked on current window.
     */
    public static native boolean beginPopupContextWindow(String strId); /*
        return ImGui::BeginPopupContextWindow(strId);
    */

    /**
     * Open+begin popup when clicked on current window.
     */
    public static native boolean beginPopupContextWindow(int imGuiPopupFlags); /*
        return ImGui::BeginPopupContextWindow(NULL, imGuiPopupFlags);
    */

    /**
     * Open+begin popup when clicked on current window.
     */
    public static native boolean beginPopupContextWindow(String strId, int imGuiPopupFlags); /*
        return ImGui::BeginPopupContextWindow(strId, imGuiPopupFlags);
    */

    /**
     * Open+begin popup when clicked in void (where there are no windows).
     */
    public static native boolean beginPopupContextVoid(); /*
        return ImGui::BeginPopupContextVoid();
     */

    /**
     * Open+begin popup when clicked in void (where there are no windows).
     */
    public static native boolean beginPopupContextVoid(String strId); /*
        return ImGui::BeginPopupContextVoid(strId);
    */

    /**
     * Open+begin popup when clicked in void (where there are no windows).
     */
    public static native boolean beginPopupContextVoid(int imGuiPopupFlags); /*
        return ImGui::BeginPopupContextVoid(NULL, imGuiPopupFlags);
    */

    /**
     * Open+begin popup when clicked in void (where there are no windows).
     */
    public static native boolean beginPopupContextVoid(String strId, int imGuiPopupFlags); /*
        return ImGui::BeginPopupContextVoid(strId, imGuiPopupFlags);
    */

    // Popups: test function
    //  - IsPopupOpen(): return true if the popup is open at the current BeginPopup() level of the popup stack.
    //  - IsPopupOpen() with ImGuiPopupFlags_AnyPopupId: return true if any popup is open at the current BeginPopup() level of the popup stack.
    //  - IsPopupOpen() with ImGuiPopupFlags_AnyPopupId + ImGuiPopupFlags_AnyPopupLevel: return true if any popup is open.

    /**
     * Return true if the popup is open.
     */
    public static native boolean isPopupOpen(String strId); /*
        return ImGui::IsPopupOpen(strId);
    */

    /**
     * Return true if the popup is open.
     */
    public static native boolean isPopupOpen(String strId, int imGuiPopupFlags); /*
        return ImGui::IsPopupOpen(strId, imGuiPopupFlags);
    */

    // Tables
    // [BETA API] API may evolve slightly! If you use this, please update to the next version when it comes out!
    // - Full-featured replacement for old Columns API.
    // - See Demo->Tables for demo code.
    // - See top of imgui_tables.cpp for general commentary.
    // - See ImGuiTableFlags_ and ImGuiTableColumnFlags_ enums for a description of available flags.
    // The typical call flow is:
    // - 1. Call BeginTable().
    // - 2. Optionally call TableSetupColumn() to submit column name/flags/defaults.
    // - 3. Optionally call TableSetupScrollFreeze() to request scroll freezing of columns/rows.
    // - 4. Optionally call TableHeadersRow() to submit a header row. Names are pulled from TableSetupColumn() data.
    // - 5. Populate contents:
    //    - In most situations you can use TableNextRow() + TableSetColumnIndex(N) to start appending into a column.
    //    - If you are using tables as a sort of grid, where every columns is holding the same type of contents,
    //      you may prefer using TableNextColumn() instead of TableNextRow() + TableSetColumnIndex().
    //      TableNextColumn() will automatically wrap-around into the next row if needed.
    //    - IMPORTANT: Comparatively to the old Columns() API, we need to call TableNextColumn() for the first column!
    //    - Summary of possible call flow:
    //        --------------------------------------------------------------------------------------------------------
    //        TableNextRow() -> TableSetColumnIndex(0) -> Text("Hello 0") -> TableSetColumnIndex(1) -> Text("Hello 1")  // OK
    //        TableNextRow() -> TableNextColumn()      -> Text("Hello 0") -> TableNextColumn()      -> Text("Hello 1")  // OK
    //                          TableNextColumn()      -> Text("Hello 0") -> TableNextColumn()      -> Text("Hello 1")  // OK: TableNextColumn() automatically gets to next row!
    //        TableNextRow()                           -> Text("Hello 0")                                               // Not OK! Missing TableSetColumnIndex() or TableNextColumn()! Text will not appear!
    //        --------------------------------------------------------------------------------------------------------
    // - 5. Call EndTable()

    public static native boolean beginTable(String id, int column); /*
        return ImGui::BeginTable(id, column);
    */

    public static native boolean beginTable(String id, int column, int imGuiTableFlags); /*
        return ImGui::BeginTable(id, column, imGuiTableFlags);
    */

    public static native boolean beginTable(String id, int column, int imGuiTableFlags, float outerSizeX, float outerSizeY); /*
        return ImGui::BeginTable(id, column, imGuiTableFlags, ImVec2(outerSizeX, outerSizeY));
    */

    public static native boolean beginTable(String id, int column, int imGuiTableFlags, float outerSizeX, float outerSizeY, float innerWidth); /*
        return ImGui::BeginTable(id, column, imGuiTableFlags, ImVec2(outerSizeX, outerSizeY), innerWidth);
    */

    /**
     * Only call EndTable() if BeginTable() returns true!
     */
    public static native void endTable(); /*
        ImGui::EndTable();
    */

    /**
     * Append into the first cell of a new row.
     */
    public static native void tableNextRow(); /*
        ImGui::TableNextRow();
    */

    /**
     * Append into the first cell of a new row.
     */
    public static native void tableNextRow(int imGuiTableRowFlags); /*
        ImGui::TableNextRow(imGuiTableRowFlags);
    */

    /**
     * Append into the first cell of a new row.
     */
    public static native void tableNextRow(int imGuiTableRowFlags, float minRowHeight); /*
        ImGui::TableNextRow(imGuiTableRowFlags, minRowHeight);
    */

    /**
     * Append into the next column (or first column of next row if currently in last column). Return true when column is visible.
     */
    public static native boolean tableNextColumn(); /*
        return ImGui::TableNextColumn();
    */

    /**
     * Append into the specified column. Return true when column is visible.
     */
    public static native boolean tableSetColumnIndex(int columnN); /*
        return ImGui::TableSetColumnIndex(columnN);
    */

    // Tables: Headers & Columns declaration
    // - Use TableSetupColumn() to specify label, resizing policy, default width/weight, id, various other flags etc.
    // - Use TableHeadersRow() to create a header row and automatically submit a TableHeader() for each column.
    //   Headers are required to perform: reordering, sorting, and opening the context menu.
    //   The context menu can also be made available in columns body using ImGuiTableFlags_ContextMenuInBody.
    // - You may manually submit headers using TableNextRow() + TableHeader() calls, but this is only useful in
    //   some advanced use cases (e.g. adding custom widgets in header row).
    // - Use TableSetupScrollFreeze() to lock columns/rows so they stay visible when scrolled.

    public static native void tableSetupColumn(String label); /*
        ImGui::TableSetupColumn(label);
    */

    public static native void tableSetupColumn(String label, int imGuiTableColumnFlags); /*
        ImGui::TableSetupColumn(label, imGuiTableColumnFlags);
    */

    public static native void tableSetupColumn(String label, int imGuiTableColumnFlags, float initWidthOrWeight); /*
        ImGui::TableSetupColumn(label, imGuiTableColumnFlags, initWidthOrWeight);
    */

    public static native void tableSetupColumn(String label, int imGuiTableColumnFlags, float initWidthOrWeight, int userId); /*
        ImGui::TableSetupColumn(label, imGuiTableColumnFlags, initWidthOrWeight, userId);
    */

    /**
     * Lock columns/rows so they stay visible when scrolled.
     */
    public static native void tableSetupScrollFreeze(int cols, int rows); /*
        ImGui::TableSetupScrollFreeze(cols, rows);
    */

    /**
     * Submit all headers cells based on data provided to TableSetupColumn() + submit context menu
     */
    public static native void tableHeadersRow(); /*
        ImGui::TableHeadersRow();
    */

    /**
     * Submit one header cell manually (rarely used)
     */
    public static native void tableHeader(String label); /*
        ImGui::TableHeader(label);
    */

    // Tables: Sorting
    // - Call TableGetSortSpecs() to retrieve latest sort specs for the table. NULL when not sorting.
    // - When 'SpecsDirty == true' you should sort your data. It will be true when sorting specs have changed
    //   since last call, or the first time. Make sure to set 'SpecsDirty = false' after sorting, else you may
    //   wastefully sort your data every frame!
    // - Lifetime: don't hold on this pointer over multiple frames or past any subsequent call to BeginTable().

    // TODO: TableGetSortSpecs()

    // Tables: Miscellaneous functions
    // - Functions args 'int column_n' treat the default value of -1 as the same as passing the current column index.

    /**
     * Return number of columns (value passed to BeginTable).
     */
    public static native int tableGetColumnCount(); /*
        return ImGui::TableGetColumnCount();
    */

    /**
     * Return current column index.
     */
    public static native int tableGetColumnIndex(); /*
        return ImGui::TableGetColumnIndex();
    */

    /**
     * Return current row index.
     */
    public static native int tableGetRowIndex(); /*
        return ImGui::TableGetRowIndex();
    */

    /**
     * Return "" if column didn't have a name declared by TableSetupColumn(). Pass -1 to use current column.
     */
    public static native String tableGetColumnName(); /*
        return env->NewStringUTF(ImGui::TableGetColumnName());
    */

    /**
     * Return "" if column didn't have a name declared by TableSetupColumn(). Pass -1 to use current column.
     */
    public static native String tableGetColumnName(int columnN); /*
        return env->NewStringUTF(ImGui::TableGetColumnName(columnN));
    */

    /**
     * Return column flags so you can query their Enabled/Visible/Sorted/Hovered status flags. Pass -1 to use current column.
     */
    public static native int tableGetColumnFlags(); /*
        return ImGui::TableGetColumnFlags();
    */

    /**
     * Return column flags so you can query their Enabled/Visible/Sorted/Hovered status flags. Pass -1 to use current column.
     */
    public static native int tableGetColumnFlags(int columnN); /*
        return ImGui::TableGetColumnFlags(columnN);
    */

    /**
     * Change the color of a cell, row, or column. See ImGuiTableBgTarget_ flags for details.
     */
    public static native void tableSetBgColor(int imGuiTableBgTarget, int color); /*
        ImGui::TableSetBgColor(imGuiTableBgTarget, color);
    */

    /**
     * Change the color of a cell, row, or column. See ImGuiTableBgTarget_ flags for details.
     */
    public static native void tableSetBgColor(int imGuiTableBgTarget, int color, int columnN); /*
        ImGui::TableSetBgColor(imGuiTableBgTarget, color, columnN);
    */

    // Legacy Columns API (2020: prefer using Tables!)
    // Columns
    // - You can also use SameLine(posX) to mimic simplified columns.

    public static native void columns(); /*
        ImGui::Columns();
    */

    public static native void columns(int count); /*
        ImGui::Columns(count);
    */

    public static native void columns(int count, String id); /*
        ImGui::Columns(count, id);
    */

    public static native void columns(int count, String id, boolean border); /*
        ImGui::Columns(count, id, border);
    */

    /**
     * Next column, defaults to current row or next row if the current row is finished
     */
    public static native void nextColumn(); /*
        ImGui::NextColumn();
    */

    /**
     * Get current column index
     */
    public static native int getColumnIndex(); /*
        return ImGui::GetColumnIndex();
     */

    /**
     * Get column width (in pixels). pass -1 to use current column
     */
    public static native float getColumnWidth(); /*
        return ImGui::GetColumnWidth();
    */

    /**
     * Get column width (in pixels). pass -1 to use current column
     */
    public static native float getColumnWidth(int columnIndex); /*
        return ImGui::GetColumnWidth(columnIndex);
    */

    /**
     * Set column width (in pixels). pass -1 to use current column
     */
    public static native void setColumnWidth(int columnIndex, float width); /*
        ImGui::SetColumnWidth(columnIndex, width);
    */

    /**
     * Get position of column line (in pixels, from the left side of the contents region). pass -1 to use current column, otherwise 0..GetColumnsCount() inclusive. column 0 is typically 0.0f
     */
    public static native float getColumnOffset(); /*
        return ImGui::GetColumnOffset();
    */

    /**
     * Get position of column line (in pixels, from the left side of the contents region). pass -1 to use current column, otherwise 0..GetColumnsCount() inclusive. column 0 is typically 0.0f
     */
    public static native float getColumnOffset(int columnIndex); /*
        return ImGui::GetColumnOffset(columnIndex);
    */

    /**
     * Set position of column line (in pixels, from the left side of the contents region). pass -1 to use current column
     */
    public static native void setColumnOffset(int columnIndex, float offsetX); /*
        ImGui::SetColumnOffset(columnIndex, offsetX);
    */

    public static native int getColumnsCount(); /*
        return ImGui::GetColumnsCount();
    */

    // Tab Bars, Tabs
    // Note: Tabs are automatically created by the docking system. Use this to create tab bars/tabs yourself without docking being involved.

    /**
     * Create and append into a TabBar
     */
    public static native boolean beginTabBar(String strId); /*
        return ImGui::BeginTabBar(strId);
    */

    /**
     * Create and append into a TabBar
     */
    public static native boolean beginTabBar(String strId, int imGuiTabBarFlags); /*
        return ImGui::BeginTabBar(strId, imGuiTabBarFlags);
    */

    /**
     * Only call EndTabBar() if BeginTabBar() returns true!
     */
    public static native void endTabBar(); /*
        ImGui::EndTabBar();
    */

    /**
     * Create a Tab. Returns true if the Tab is selected.
     */
    public static native boolean beginTabItem(String label); /*
        return ImGui::BeginTabItem(label);
    */

    /**
     * Create a Tab. Returns true if the Tab is selected.
     */
    public static boolean beginTabItem(String label, ImBoolean pOpen) {
        return nBeginTabItem(label, pOpen.getData(), 0);
    }

    /**
     * Create a Tab. Returns true if the Tab is selected.
     */
    public static boolean beginTabItem(String label, int imGuiTabItemFlags) {
        return nBeginTabItem(label, imGuiTabItemFlags);
    }

    /**
     * Create a Tab. Returns true if the Tab is selected.
     */
    public static boolean beginTabItem(String label, ImBoolean pOpen, int imGuiTabItemFlags) {
        return nBeginTabItem(label, pOpen.getData(), imGuiTabItemFlags);
    }

    private static native boolean nBeginTabItem(String label, int imGuiTabItemFlags); /*
        return ImGui::BeginTabItem(label, NULL, imGuiTabItemFlags);
    */

    private static native boolean nBeginTabItem(String label, boolean[] pOpen, int imGuiTabItemFlags); /*
        return ImGui::BeginTabItem(label, &pOpen[0], imGuiTabItemFlags);
    */

    /**
     * Only call EndTabItem() if BeginTabItem() returns true!
     */
    public static native void endTabItem(); /*
        ImGui::EndTabItem();
    */

    /**
     * Create a Tab behaving like a button. return true when clicked. cannot be selected in the tab bar.
     */
    public static native boolean tabItemButton(String label); /*
        return ImGui::TabItemButton(label);
    */

    /**
     * Create a Tab behaving like a button. return true when clicked. cannot be selected in the tab bar.
     */
    public static native boolean tabItemButton(String label, int imGuiTabItemFlags); /*
        return ImGui::TabItemButton(label, imGuiTabItemFlags);
    */

    /**
     * Notify TabBar or Docking system of a closed tab/window ahead (useful to reduce visual flicker on reorderable tab bars).
     * For tab-bar: call after BeginTabBar() and before Tab submissions. Otherwise call with a window name.
     */
    public static native void setTabItemClosed(String tabOrDockedWindowLabel); /*
        ImGui::SetTabItemClosed(tabOrDockedWindowLabel);
    */

    // Docking
    // [BETA API] Enable with io.ConfigFlags |= ImGuiConfigFlags_DockingEnable.
    // Note: You can use most Docking facilities without calling any API. You DO NOT need to call DockSpace() to use Docking!
    // - To dock windows: if io.ConfigDockingWithShift == false (default) drag window from their title bar.
    // - To dock windows: if io.ConfigDockingWithShift == true: hold SHIFT anywhere while moving windows.
    // About DockSpace:
    // - Use DockSpace() to create an explicit dock node _within_ an existing window. See Docking demo for details.
    // - DockSpace() needs to be submitted _before_ any window they can host. If you use a dockspace, submit it early in your app.

    public static int dockSpace(int imGuiID) {
        return nDockSpace(imGuiID, 0, 0, 0, 0);
    }

    public static int dockSpace(int imGuiID, float sizeX, float sizeY) {
        return nDockSpace(imGuiID, sizeX, sizeY, 0, 0);
    }

    public static int dockSpace(int imGuiID, float sizeX, float sizeY, int imGuiDockNodeFlags) {
        return nDockSpace(imGuiID, sizeX, sizeY, imGuiDockNodeFlags, 0);
    }

    public static int dockSpace(int imGuiID, float sizeX, float sizeY, int imGuiDockNodeFlags, ImGuiWindowClass imGuiWindowClass) {
        return nDockSpace(imGuiID, sizeX, sizeY, imGuiDockNodeFlags, imGuiWindowClass.ptr);
    }

    private static native int nDockSpace(int imGuiID, float sizeX, float sizeY, int imGuiDockNodeFlags, long windowClassPtr); /*
        return ImGui::DockSpace(imGuiID, ImVec2(sizeX, sizeY), imGuiDockNodeFlags, windowClassPtr != 0 ? (ImGuiWindowClass*)windowClassPtr : NULL);
    */

    public static int dockSpaceOverViewport() {
        return nDockSpaceOverViewport(0, 0, 0);
    }

    public static int dockSpaceOverViewport(ImGuiViewport viewport) {
        return nDockSpaceOverViewport(viewport.ptr, 0, 0);
    }

    public static int dockSpaceOverViewport(ImGuiViewport viewport, int imGuiDockNodeFlags) {
        return nDockSpaceOverViewport(viewport.ptr, imGuiDockNodeFlags, 0);
    }

    public static int dockSpaceOverViewport(ImGuiViewport viewport, int imGuiDockNodeFlags, ImGuiWindowClass windowClass) {
        return nDockSpaceOverViewport(viewport.ptr, imGuiDockNodeFlags, windowClass.ptr);
    }

    private static native int nDockSpaceOverViewport(long viewportPtr, int imGuiDockNodeFlags, long windowClassPtr); /*
        return ImGui::DockSpaceOverViewport(viewportPtr != 0 ? (ImGuiViewport*)viewportPtr : NULL, imGuiDockNodeFlags, windowClassPtr != 0 ? (ImGuiWindowClass*)windowClassPtr : NULL);
    */

    /**
     * Set next window dock id
     */
    public static native void setNextWindowDockID(int dockId); /*
        ImGui::SetNextWindowDockID(dockId);
    */

    /**
     * Set next window dock id
     */
    public static native void setNextWindowDockID(int dockId, int imGuiCond); /*
        ImGui::SetNextWindowDockID(dockId, imGuiCond);
    */

    /**
     * set next window class (rare/advanced uses: provide hints to the platform backend via altered viewport flags and parent/child info)
     */
    public static void setNextWindowClass(ImGuiWindowClass windowClass) {
        nSetNextWindowClass(windowClass.ptr);
    }

    private static native void nSetNextWindowClass(long windowClassPtr); /*
        ImGui::SetNextWindowClass((ImGuiWindowClass*)windowClassPtr);
    */

    public static native int getWindowDockID(); /*
        return ImGui::GetWindowDockID();
    */

    /**
     * Is current window docked into another window?
     */
    public static native boolean isWindowDocked(); /*
        return ImGui::IsWindowDocked();
    */

    // Logging/Capture
    // - All text output from the interface can be captured into tty/file/clipboard. By default, tree nodes are automatically opened during logging.

    /**
     * Start logging to tty (stdout)
     */
    public static native void logToTTY(); /*
        ImGui::LogToTTY();
    */

    /**
     * Start logging to tty (stdout)
     */
    public static native void logToTTY(int autoOpenDepth); /*
        ImGui::LogToTTY(autoOpenDepth);
    */

    /**
     * Start logging to file
     */
    public static native void logToFile(); /*
        ImGui::LogToFile();
    */

    /**
     * Start logging to file
     */
    public static native void logToFile(int autoOpenDepth); /*
        ImGui::LogToFile(autoOpenDepth);
    */

    /**
     * Start logging to file
     */
    public static native void logToFile(int autoOpenDepth, String filename); /*
        ImGui::LogToFile(autoOpenDepth, filename);
    */

    /**
     * Start logging to OS clipboard
     */
    public static native void logToClipboard(); /*
        ImGui::LogToClipboard();
    */


    /**
     * Start logging to OS clipboard
     */
    public static native void logToClipboard(int autoOpenDepth); /*
        ImGui::LogToClipboard(autoOpenDepth);
    */

    /**
     * Stop logging (close file, etc.)
     */
    public static native void logFinish(); /*
        ImGui::LogFinish();
    */

    /**
     * Helper to display buttons for logging to tty/file/clipboard
     */
    public static native void logButtons(); /*
        ImGui::LogButtons();
    */

    /**
     * Pass text data straight to log (without being displayed)
     */
    public static native void logText(String text); /*
        ImGui::LogText(text, NULL);
    */

    // Drag and Drop
    // - If you stop calling BeginDragDropSource() the payload is preserved however it won't have a preview tooltip (we currently display a fallback "..." tooltip as replacement)

    private static WeakReference<Object> payloadRef = null;
    private static final byte[] PAYLOAD_PLACEHOLDER_DATA = new byte[1];

    /**
     * Call when the current item is active. If this return true, you can call SetDragDropPayload() + EndDragDropSource()
     */
    public static native boolean beginDragDropSource(); /*
        return ImGui::BeginDragDropSource();
    */

    /**
     * Call when the current item is active. If this return true, you can call SetDragDropPayload() + EndDragDropSource()
     */
    public static native boolean beginDragDropSource(int imGuiDragDropFlags); /*
        return ImGui::BeginDragDropSource(imGuiDragDropFlags);
    */

    /**
     * Type is a user defined string of maximum 32 characters. Strings starting with '_' are reserved for dear imgui internal types.
     * <p>
     * BINDING NOTICE:
     * Method adopted for Java, so objects are used instead of raw bytes.
     * Binding stores a reference to the object in a form of {@link WeakReference}.
     */
    public static boolean setDragDropPayload(final String dataType, final Object payload) {
        return setDragDropPayload(dataType, payload, ImGuiCond.None);
    }

    /**
     * Type is a user defined string of maximum 32 characters. Strings starting with '_' are reserved for dear imgui internal types.
     * <p>
     * BINDING NOTICE:
     * Method adopted for Java, so objects are used instead of raw bytes.
     * Binding stores a reference to the object in a form of {@link WeakReference}.
     */
    public static boolean setDragDropPayload(final String dataType, final Object payload, final int imGuiCond) {
        if (payloadRef == null || payloadRef.get() != payload) {
            payloadRef = new WeakReference<>(payload);
        }
        return nSetDragDropPayload(dataType, PAYLOAD_PLACEHOLDER_DATA, 1, imGuiCond);
    }

    /**
     * Binding alternative for {@link #setDragDropPayload(String, Object)}, which uses payload class as a unique identifier.
     */
    public static boolean setDragDropPayload(final Object payload) {
        return setDragDropPayload(payload, ImGuiCond.None);
    }

    /**
     * Binding alternative for {@link #setDragDropPayload(String, Object, int)}, which uses payload class as a unique identifier.
     */
    public static boolean setDragDropPayload(final Object payload, final int imGuiCond) {
        return setDragDropPayload(String.valueOf(payload.getClass().hashCode()), payload, imGuiCond);
    }

    private static native boolean nSetDragDropPayload(String dataType, byte[] data, int sz, int imGuiCond); /*
        return ImGui::SetDragDropPayload(dataType, &data[0], sz, imGuiCond);
    */

    /**
     * Only call EndDragDropSource() if BeginDragDropSource() returns true!
     */
    public static native void endDragDropSource(); /*
        ImGui::EndDragDropSource();
    */

    /**
     * Call after submitting an item that may receive a payload. If this returns true, you can call AcceptDragDropPayload() + EndDragDropTarget()
     */
    public static native boolean beginDragDropTarget(); /*
        return ImGui::BeginDragDropTarget();
    */

    /**
     * Accept contents of a given type. If ImGuiDragDropFlags_AcceptBeforeDelivery is set you can peek into the payload before the mouse button is released.
     */
    public static <T> T acceptDragDropPayload(final String dataType) {
        return acceptDragDropPayload(dataType, ImGuiDragDropFlags.None);
    }

    /**
     * Type safe alternative for {@link #acceptDragDropPayload(String)}, since it checks assignability of the accepted class.
     */
    public static <T> T acceptDragDropPayload(final String dataType, final Class<T> aClass) {
        return acceptDragDropPayload(dataType, ImGuiDragDropFlags.None, aClass);
    }

    /**
     * Accept contents of a given type. If ImGuiDragDropFlags_AcceptBeforeDelivery is set you can peek into the payload before the mouse button is released.
     */
    public static <T> T acceptDragDropPayload(final String dataType, final int imGuiDragDropFlags) {
        return acceptDragDropPayload(dataType, imGuiDragDropFlags, null);
    }

    /**
     * Type safe alternative for {@link #acceptDragDropPayload(String, int)}, since it checks assignability of the accepted class.
     */
    @SuppressWarnings("unchecked")
    public static <T> T acceptDragDropPayload(final String dataType, final int imGuiDragDropFlags, final Class<T> aClass) {
        if (payloadRef != null && nAcceptDragDropPayload(dataType, imGuiDragDropFlags)) {
            final Object rawPayload = payloadRef.get();
            if (rawPayload != null) {
                if (aClass == null || rawPayload.getClass().isAssignableFrom(aClass)) {
                    return (T) rawPayload;
                }
            }
        }
        return null;
    }

    /**
     * Binding alternative for {@link #acceptDragDropPayload(String)}, which uses payload class as a unique identifier.
     */
    public static <T> T acceptDragDropPayload(final Class<T> aClass) {
        return acceptDragDropPayload(String.valueOf(aClass.hashCode()), ImGuiDragDropFlags.None, aClass);
    }

    /**
     * Binding alternative for {@link #acceptDragDropPayload(String, int)}, which uses payload class as a unique identifier.
     */
    public static <T> T acceptDragDropPayload(final Class<T> aClass, final int imGuiDragDropFlags) {
        return acceptDragDropPayload(String.valueOf(aClass.hashCode()), imGuiDragDropFlags, aClass);
    }

    private static native boolean nAcceptDragDropPayload(String dataType, int imGuiDragDropFlags); /*
        return ImGui::AcceptDragDropPayload(dataType, imGuiDragDropFlags) != NULL;
    */

    /**
     * Only call EndDragDropTarget() if BeginDragDropTarget() returns true!
     */
    public static native void endDragDropTarget(); /*
        ImGui::EndDragDropTarget();
    */

    /**
     * Peek directly into the current payload from anywhere. May return NULL.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDragDropPayload() {
        if (payloadRef != null && nHasDragDropPayload()) {
            final Object rawPayload = payloadRef.get();
            if (rawPayload != null) {
                return (T) rawPayload;
            }
        }
        return null;
    }

    /**
     * Peek directly into the current payload from anywhere. May return NULL. Checks if payload has the same type as provided.
     */
    @SuppressWarnings("unchecked")
    public static <T> T getDragDropPayload(final String dataType) {
        if (payloadRef != null && nHasDragDropPayload(dataType)) {
            final Object rawPayload = payloadRef.get();
            if (rawPayload != null) {
                return (T) rawPayload;
            }
        }
        return null;
    }

    /**
     * Binding alternative for {@link #getDragDropPayload(String)}, which uses payload class as a unique identifier.
     */
    public static <T> T getDragDropPayload(final Class<T> aClass) {
        return getDragDropPayload(String.valueOf(aClass.hashCode()));
    }

    private static native boolean nHasDragDropPayload(); /*
        return ImGui::GetDragDropPayload()->Data != NULL;
    */

    private static native boolean nHasDragDropPayload(String dataType); /*
        return ImGui::GetDragDropPayload()->IsDataType(dataType);
    */

    /**
     * Disable all user interactions and dim items visuals (applying style.DisabledAlpha over current colors)
     * BeginDisabled(false) essentially does nothing useful but is provided to facilitate use of boolean expressions.
     * If you can avoid calling BeginDisabled(False)/EndDisabled() best to avoid it.
     */
    public static void beginDisabled() {
        beginDisabled(true);
    }

    /**
     * Disable all user interactions and dim items visuals (applying style.DisabledAlpha over current colors)
     * BeginDisabled(false) essentially does nothing useful but is provided to facilitate use of boolean expressions.
     * If you can avoid calling BeginDisabled(False)/EndDisabled() best to avoid it.
     */
    public static native void beginDisabled(boolean disabled); /*
        ImGui::BeginDisabled(disabled);
    */

    public static native void endDisabled(); /*
        ImGui::EndDisabled();
    */

    // Clipping
    // - Mouse hovering is affected by ImGui::PushClipRect() calls, unlike direct calls to ImDrawList::PushClipRect() which are render only.

    public static native void pushClipRect(float clipRectMinX, float clipRectMinY, float clipRectMaxX, float clipRectMaxY, boolean intersectWithCurrentClipRect); /*
        ImGui::PushClipRect(ImVec2(clipRectMinX, clipRectMinY), ImVec2(clipRectMaxX, clipRectMaxY), intersectWithCurrentClipRect);
    */

    public static native void popClipRect(); /*
        ImGui::PopClipRect();
    */

    // Focus, Activation
    // - Prefer using "SetItemDefaultFocus()" over "if (IsWindowAppearing()) SetScrollHereY()" when applicable to signify "this is the default item"

    /**
     * Make last item the default focused item of a window.
     */
    public static native void setItemDefaultFocus(); /*
        ImGui::SetItemDefaultFocus();
    */

    /**
     * Focus keyboard on the next widget. Use positive 'offset' to access sub components of a multiple component widget. Use -1 to access previous widget.
     */
    public static native void setKeyboardFocusHere(); /*
        ImGui::SetKeyboardFocusHere();
    */

    /**
     * Focus keyboard on the next widget. Use positive 'offset' to access sub components of a multiple component widget. Use -1 to access previous widget.
     */
    public static native void setKeyboardFocusHere(int offset); /*
        ImGui::SetKeyboardFocusHere(offset);
    */

    // Item/Widgets Utilities
    // - Most of the functions are referring to the last/previous item we submitted.
    // - See Demo Window under "Widgets->Querying Status" for an interactive visualization of most of those functions.

    /**
     * Is the last item hovered? (and usable, aka not blocked by a popup, etc.). See ImGuiHoveredFlags for more options.
     */
    public static native boolean isItemHovered(); /*
        return ImGui::IsItemHovered();
    */

    /**
     * Is the last item hovered? (and usable, aka not blocked by a popup, etc.). See ImGuiHoveredFlags for more options.
     */
    public static native boolean isItemHovered(int imGuiHoveredFlags); /*
        return ImGui::IsItemHovered(imGuiHoveredFlags);
    */

    /**
     * Is the last item active? (e.g. button being held, text field being edited.
     * This will continuously return true while holding mouse button on an item.
     * Items that don't interact will always return false)
     */
    public static native boolean isItemActive(); /*
        return ImGui::IsItemActive();
    */

    /**
     * Is the last item focused for keyboard/gamepad navigation?
     */
    public static native boolean isItemFocused(); /*
        return ImGui::IsItemFocused();
    */

    /**
     * Is the last item clicked? (e.g. button/node just clicked on) == {@code IsMouseClicked(mouseButton) && IsItemHovered()}
     */
    public static native boolean isItemClicked(); /*
        return ImGui::IsItemClicked();
    */

    /**
     * Is the last item clicked? (e.g. button/node just clicked on) == {@code IsMouseClicked(mouseButton) && IsItemHovered()}
     */
    public static native boolean isItemClicked(int mouseButton); /*
        return ImGui::IsItemClicked(mouseButton);
    */

    /**
     * Is the last item visible? (items may be out of sight because of clipping/scrolling)
     */
    public static native boolean isItemVisible(); /*
        return ImGui::IsItemVisible();
    */

    /**
     * Did the last item modify its underlying value this frame? or was pressed? This is generally the same as the "bool" return value of many widgets.
     */
    public static native boolean isItemEdited(); /*
        return ImGui::IsItemEdited();
    */

    /**
     * Was the last item just made active (item was previously inactive).
     */
    public static native boolean isItemActivated(); /*
        return ImGui::IsItemActivated();
    */

    /**
     * Was the last item just made inactive (item was previously active). Useful for Undo/Redo patterns with widgets that requires continuous editing.
     */
    public static native boolean isItemDeactivated(); /*
        return ImGui::IsItemDeactivated();
    */

    /**
     * Was the last item just made inactive and made a value change when it was active? (e.g. Slider/Drag moved).
     * Useful for Undo/Redo patterns with widgets that requires continuous editing.
     * Note that you may get false positives (some widgets such as Combo()/ListBox()/Selectable() will return true even when clicking an already selected item).
     */
    public static native boolean isItemDeactivatedAfterEdit(); /*
        return ImGui::IsItemDeactivatedAfterEdit();
    */

    /**
     * Was the last item open state toggled? set by TreeNode().
     */
    public static native boolean isItemToggledOpen(); /*
        return ImGui::IsItemToggledOpen();
    */

    /**
     * Is any item hovered?
     */
    public static native boolean isAnyItemHovered(); /*
        return ImGui::IsAnyItemHovered();
    */

    /**
     * Is any item active?
     */
    public static native boolean isAnyItemActive(); /*
        return ImGui::IsAnyItemActive();
    */

    /**
     * Is any item focused?
     */
    public static native boolean isAnyItemFocused(); /*
        return ImGui::IsAnyItemFocused();
    */

    /**
     * Get upper-left bounding rectangle of the last item (screen space)
     */
    public static ImVec2 getItemRectMin() {
        final ImVec2 value = new ImVec2();
        getItemRectMin(value);
        return value;
    }

    /**
     * Get upper-left bounding rectangle of the last item (screen space)
     */
    public static native void getItemRectMin(ImVec2 dstImVec2); /*
        Jni::ImVec2Cpy(env, ImGui::GetItemRectMin(), dstImVec2);
    */

    /**
     * Get upper-left bounding rectangle of the last item (screen space)
     */
    public static native float getItemRectMinX(); /*
        return ImGui::GetItemRectMin().x;
    */

    /**
     * Get upper-left bounding rectangle of the last item (screen space)
     */
    public static native float getItemRectMinY(); /*
        return ImGui::GetItemRectMin().y;
    */

    /**
     * Get lower-right bounding rectangle of the last item (screen space)
     */
    public static ImVec2 getItemRectMax() {
        final ImVec2 value = new ImVec2();
        getItemRectMax(value);
        return value;
    }

    /**
     * Get lower-right bounding rectangle of the last item (screen space)
     */
    public static native void getItemRectMax(ImVec2 dstImVec2); /*
        Jni::ImVec2Cpy(env, ImGui::GetItemRectMax(), dstImVec2);
    */

    /**
     * Get lower-right bounding rectangle of the last item (screen space)
     */
    public static native float getItemRectMaxX(); /*
        return ImGui::GetItemRectMax().x;
    */

    /**
     * Get lower-right bounding rectangle of the last item (screen space)
     */
    public static native float getItemRectMaxY(); /*
        return ImGui::GetItemRectMax().y;
    */

    /**
     * Get size of last item
     */
    public static ImVec2 getItemRectSize() {
        final ImVec2 value = new ImVec2();
        getItemRectSize(value);
        return value;
    }

    /**
     * Get size of last item
     */
    public static native void getItemRectSize(ImVec2 dstImVec2); /*
        Jni::ImVec2Cpy(env, ImGui::GetItemRectSize(), dstImVec2);
    */

    /**
     * Get size of last item
     */
    public static native float getItemRectSizeX(); /*
        return ImGui::GetItemRectSize().x;
    */

    /**
     * Get size of last item
     */
    public static native float getItemRectSizeY(); /*
        return ImGui::GetItemRectSize().y;
    */

    /**
     * Allow last item to be overlapped by a subsequent item. sometimes useful with invisible buttons, selectables, etc. to catch unused area.
     */
    public static native void setItemAllowOverlap(); /*
        ImGui::SetItemAllowOverlap();
    */

    // Viewports
    // - Currently represents the Platform Window created by the application which is hosting our Dear ImGui windows.
    // - In 'docking' branch with multi-viewport enabled, we extend this concept to have multiple active viewports.
    // - In the future we will extend this concept further to also represent Platform Monitor and support a "no main platform window" operation mode.

    /**
     * Return primary/default viewport.
     */
    public static ImGuiViewport getMainViewport() {
        MAIN_VIEWPORT.ptr = nGetMainViewport();
        return MAIN_VIEWPORT;
    }

    private static native long nGetMainViewport(); /*
        return (intptr_t)ImGui::GetMainViewport();
    */

    // Miscellaneous Utilities

    /**
     * Test if rectangle (of given size, starting from cursor position) is visible / not clipped.
     */
    public static native boolean isRectVisible(float width, float height); /*
        return ImGui::IsRectVisible(ImVec2(width, height));
    */

    /**
     * Test if rectangle (in screen space) is visible / not clipped. to perform coarse clipping on user's side.
     */
    public static native boolean isRectVisible(float minX, float minY, float maxX, float maxY); /*
        return ImGui::IsRectVisible(ImVec2(minX, minY), ImVec2(maxX, maxY));
    */

    /**
     * Get global imgui time. incremented by io.DeltaTime every frame.
     */
    public static native double getTime(); /*
        return ImGui::GetTime();
    */

    /**
     * Get global imgui frame count. incremented by 1 every frame.
     */
    public static native int getFrameCount(); /*
        return ImGui::GetFrameCount();
    */

    /**
     * Get background draw list for the viewport associated to the current window.
     * This draw list will be the first rendering one. Useful to quickly draw shapes/text behind dear imgui contents.
     */
    public static ImDrawList getBackgroundDrawList() {
        BACKGROUND_DRAW_LIST.ptr = nGetBackgroundDrawList();
        return BACKGROUND_DRAW_LIST;
    }

    private static native long nGetBackgroundDrawList(); /*
        return (intptr_t)ImGui::GetBackgroundDrawList();
    */

    /**
     * Get foreground draw list for the viewport associated to the current window.
     * This draw list will be the first rendering one. Useful to quickly draw shapes/text behind dear imgui contents.
     */
    public static ImDrawList getForegroundDrawList() {
        FOREGROUND_DRAW_LIST.ptr = nGetForegroundDrawList();
        return FOREGROUND_DRAW_LIST;
    }

    private static native long nGetForegroundDrawList(); /*
        return (intptr_t)ImGui::GetForegroundDrawList();
    */

    /**
     * Get background draw list for the given viewport.
     * This draw list will be the first rendering one. Useful to quickly draw shapes/text behind dear imgui contents.
     */
    public static ImDrawList getBackgroundDrawList(ImGuiViewport viewport) {
        BACKGROUND_DRAW_LIST.ptr = nGetBackgroundDrawList(viewport.ptr);
        return BACKGROUND_DRAW_LIST;
    }

    private static native long nGetBackgroundDrawList(long viewportPtr); /*
        return (intptr_t)ImGui::GetBackgroundDrawList((ImGuiViewport*)viewportPtr);
    */

    /**
     * Get foreground draw list for the given viewport.
     * This draw list will be the last rendered one. Useful to quickly draw shapes/text over dear imgui contents.
     */
    public static ImDrawList getForegroundDrawList(ImGuiViewport viewport) {
        BACKGROUND_DRAW_LIST.ptr = nGetForegroundDrawList(viewport.ptr);
        return BACKGROUND_DRAW_LIST;
    }

    private static native long nGetForegroundDrawList(long viewportPtr); /*
        return (intptr_t)ImGui::GetBackgroundDrawList((ImGuiViewport*)viewportPtr);
    */

    // TODO GetDrawListSharedData

    /**
     * Get a string corresponding to the enum value (for display, saving, etc.).
     */
    public static native String getStyleColorName(int imGuiCol); /*
        return env->NewStringUTF(ImGui::GetStyleColorName(imGuiCol));
    */

    /**
     * Replace current window storage with our own (if you want to manipulate it yourself, typically clear subsection of it).
     */
    public static void setStateStorage(final ImGuiStorage storage) {
        nSetStateStorage(storage.ptr);
    }

    private static native void nSetStateStorage(long imGuiStoragePtr); /*
        ImGui::SetStateStorage((ImGuiStorage*)imGuiStoragePtr);
    */

    public static ImGuiStorage getStateStorage() {
        IMGUI_STORAGE.ptr = nGetStateStorage();
        return IMGUI_STORAGE;
    }

    private static native long nGetStateStorage(); /*
        return (intptr_t)ImGui::GetStateStorage();
    */

    /**
     * Helper to create a child window / scrolling region that looks like a normal widget frame
     */
    public static native boolean beginChildFrame(int id, float width, float height); /*
        return ImGui::BeginChildFrame(id, ImVec2(width, height));
    */

    /**
     * Helper to create a child window / scrolling region that looks like a normal widget frame
     */
    public static native boolean beginChildFrame(int id, float width, float height, int imGuiWindowFlags); /*
        return ImGui::BeginChildFrame(id, ImVec2(width, height), imGuiWindowFlags);
    */

    /**
     * Always call EndChildFrame() regardless of BeginChildFrame() return values (which indicates a collapsed/clipped window)
     */
    public static native void endChildFrame(); /*
        ImGui::EndChildFrame();
    */

    // Text Utilities

    public final ImVec2 calcTextSize(final String text) {
        final ImVec2 value = new ImVec2();
        calcTextSize(value, text);
        return value;
    }

    public final ImVec2 calcTextSize(final String text, final boolean hideTextAfterDoubleHash) {
        final ImVec2 value = new ImVec2();
        calcTextSize(value, text, hideTextAfterDoubleHash);
        return value;
    }

    public final ImVec2 calcTextSize(final String text, final boolean hideTextAfterDoubleHash, final float wrapWidth) {
        final ImVec2 value = new ImVec2();
        calcTextSize(value, text, hideTextAfterDoubleHash, wrapWidth);
        return value;
    }

    public static native void calcTextSize(ImVec2 dstImVec2, String text); /*
        ImVec2 src = ImGui::CalcTextSize(text);
        Jni::ImVec2Cpy(env, src, dstImVec2);
    */

    public static native void calcTextSize(ImVec2 dstImVec2, String text, boolean hideTextAfterDoubleHash); /*
        ImVec2 src = ImGui::CalcTextSize(text, NULL, hideTextAfterDoubleHash);
        Jni::ImVec2Cpy(env, src, dstImVec2);
    */

    public static native void calcTextSize(ImVec2 dstImVec2, String text, float wrapWidth); /*
        ImVec2 src = ImGui::CalcTextSize(text, NULL, false, wrapWidth);
        Jni::ImVec2Cpy(env, src, dstImVec2);
    */

    public static native void calcTextSize(ImVec2 dstImVec2, String text, boolean hideTextAfterDoubleHash, float wrapWidth); /*
        ImVec2 src = ImGui::CalcTextSize(text, NULL, hideTextAfterDoubleHash, wrapWidth);
        Jni::ImVec2Cpy(env, src, dstImVec2);
    */

    // Color Utilities

    public final ImVec4 colorConvertU32ToFloat4(final int in) {
        final ImVec4 value = new ImVec4();
        colorConvertU32ToFloat4(in, value);
        return value;
    }

    public static native void colorConvertU32ToFloat4(int in, ImVec4 dstImVec4); /*
        Jni::ImVec4Cpy(env, ImGui::ColorConvertU32ToFloat4(in), dstImVec4);
    */

    public static native int colorConvertFloat4ToU32(float r, float g, float b, float a); /*
        return ImGui::ColorConvertFloat4ToU32(ImVec4(r, g, b, a));
    */

    public static native void colorConvertRGBtoHSV(float[] rgb, float[] hsv); /*
        ImGui::ColorConvertRGBtoHSV(rgb[0], rgb[1], rgb[2], hsv[0], hsv[1], hsv[2]);
    */

    public static native void colorConvertHSVtoRGB(float[] hsv, float[] rgb); /*
        ImGui::ColorConvertHSVtoRGB(hsv[0], hsv[1], hsv[2], rgb[0], rgb[1], rgb[2]);
    */

    // Inputs Utilities: Keyboard
    // - For 'int user_key_index' you can use your own indices/enums according to how your backend/engine stored them in io.KeysDown[].
    // - We don't know the meaning of those value. You can use GetKeyIndex() to map a ImGuiKey_ value into the user index.

    /**
     * Map ImGuiKey_* values into user's key index. == io.KeyMap[key]
     */
    public static native int getKeyIndex(int imguiKey); /*
        return ImGui::GetKeyIndex(imguiKey);
    */

    /**
     * Is key being held. == io.KeysDown[user_key_index].
     */
    public static native boolean isKeyDown(int userKeyIndex); /*
        return ImGui::IsKeyDown(userKeyIndex);
    */

    /**
     * Was key pressed (went from !Down to Down)? if repeat=true, uses io.KeyRepeatDelay / KeyRepeatRate
     */
    public static native boolean isKeyPressed(int userKeyIndex); /*
        return ImGui::IsKeyPressed(userKeyIndex);
    */

    /**
     * Was key pressed (went from !Down to Down)? if repeat=true, uses io.KeyRepeatDelay / KeyRepeatRate
     */
    public static native boolean isKeyPressed(int userKeyIndex, boolean repeat); /*
        return ImGui::IsKeyPressed(userKeyIndex, repeat);
    */

    /**
     * Was key released (went from Down to !Down)..
     */
    public static native boolean isKeyReleased(int userKeyIndex); /*
        return ImGui::IsKeyReleased(userKeyIndex);
    */

    /**
     * Uses provided repeat rate/delay.
     * Return a count, most often 0 or 1 but might be {@code >1} if RepeatRate is small enough that {@code DeltaTime > RepeatRate}
     */
    public static native boolean getKeyPressedAmount(int keyIndex, float repeatDelay, float rate); /*
        return ImGui::GetKeyPressedAmount(keyIndex, repeatDelay, rate);
    */

    /**
     * Attention: misleading name! manually override io.WantCaptureKeyboard flag next frame (said flag is entirely left for your application to handle).
     * e.g. force capture keyboard when your widget is being hovered.
     * This is equivalent to setting "io.WantCaptureKeyboard = wantCaptureKeyboardValue"; after the next NewFrame() call.
     */
    public static native void captureKeyboardFromApp(); /*
        ImGui::CaptureKeyboardFromApp();
    */

    /**
     * Attention: misleading name! manually override io.WantCaptureKeyboard flag next frame (said flag is entirely left for your application to handle).
     * e.g. force capture keyboard when your widget is being hovered.
     * This is equivalent to setting "io.WantCaptureKeyboard = wantCaptureKeyboardValue"; after the next NewFrame() call.
     */
    public static native void captureKeyboardFromApp(boolean wantCaptureKeyboardValue); /*
        ImGui::CaptureKeyboardFromApp(wantCaptureKeyboardValue);
    */

    // Inputs Utilities: Mouse
    // - To refer to a mouse button, you may use named enums in your code e.g. ImGuiMouseButton_Left, ImGuiMouseButton_Right.
    // - You can also use regular integer: it is forever guaranteed that 0=Left, 1=Right, 2=Middle.
    // - Dragging operations are only reported after mouse has moved a certain distance away from the initial clicking position (see 'lock_threshold' and 'io.MouseDraggingThreshold')

    /**
     * Is mouse button held (0=left, 1=right, 2=middle)
     */
    public static native boolean isMouseDown(int button); /*
        return ImGui::IsMouseDown(button);
    */

    /**
     * Is any mouse button held
     */
    public static native boolean isAnyMouseDown(); /*
        return ImGui::IsAnyMouseDown();
    */

    /**
     * Did mouse button clicked (went from !Down to Down) (0=left, 1=right, 2=middle)
     */
    public static native boolean isMouseClicked(int button); /*
        return ImGui::IsMouseClicked(button);
    */

    public static native boolean isMouseClicked(int button, boolean repeat); /*
        return ImGui::IsMouseClicked(button, repeat);
    */

    /**
     * did mouse button double-clicked? (note that a double-click will also report IsMouseClicked() == true).
     */
    public static native boolean isMouseDoubleClicked(int button); /*
        return ImGui::IsMouseDoubleClicked(button);
    */

    /**
     * Return the number of successive mouse-clicks at the time where a click happen (otherwise 0).
     */
    public static native int getMouseClickedCount(int button); /*
        return ImGui::GetMouseClickedCount(button);
    */

    /**
     * Did mouse button released (went from Down to !Down)
     */
    public static native boolean isMouseReleased(int button); /*
        return ImGui::IsMouseReleased(button);
    */

    /**
     * Is mouse dragging. if lockThreshold {@code < -1.0f} uses io.MouseDraggingThreshold
     */
    public static native boolean isMouseDragging(int button); /*
        return ImGui::IsMouseDragging(button);
    */

    /**
     * Is mouse dragging. if lockThreshold {@code < -1.0f} uses io.MouseDraggingThreshold
     */
    public static native boolean isMouseDragging(int button, float lockThreshold); /*
        return ImGui::IsMouseDragging(button, lockThreshold);
    */

    /**
     * Is mouse hovering given bounding rect (in screen space). clipped by current clipping settings, but disregarding of other consideration of focus/window ordering/popup-block.
     */
    public static native boolean isMouseHoveringRect(float minX, float minY, float maxX, float maxY); /*
        return ImGui::IsMouseHoveringRect(ImVec2(minX, minY), ImVec2(maxX, maxY));
    */

    /**
     * Is mouse hovering given bounding rect (in screen space). clipped by current clipping settings, but disregarding of other consideration of focus/window ordering/popup-block.
     */
    public static native boolean isMouseHoveringRect(float minX, float minY, float maxX, float maxY, boolean clip); /*
        return ImGui::IsMouseHoveringRect(ImVec2(minX, minY), ImVec2(maxX, maxY), clip);
    */

    /**
     * By convention we use (-FLT_MAX,-FLT_MAX) to denote that there is no mouse
     */
    public static native boolean isMousePosValid(); /*
        return ImGui::IsMousePosValid();
    */

    /**
     * By convention we use (-FLT_MAX,-FLT_MAX) to denote that there is no mouse
     */
    public static native boolean isMousePosValid(float mousePosX, float mousePosY); /*
        ImVec2 pos = ImVec2(mousePosX, mousePosY);
        return ImGui::IsMousePosValid(&pos);
    */

    /**
     * Shortcut to ImGui::GetIO().MousePos provided by user, to be consistent with other calls
     */
    public static ImVec2 getMousePos() {
        final ImVec2 value = new ImVec2();
        getMousePos(value);
        return value;
    }

    /**
     * Shortcut to ImGui::GetIO().MousePos provided by user, to be consistent with other calls
     */
    public static native void getMousePos(ImVec2 dstImVec2); /*
        Jni::ImVec2Cpy(env, ImGui::GetMousePos(), dstImVec2);
    */

    /**
     * Shortcut to ImGui::GetIO().MousePos provided by user, to be consistent with other calls
     */
    public static native float getMousePosX(); /*
        return ImGui::GetMousePos().x;
    */

    /**
     * Shortcut to ImGui::GetIO().MousePos provided by user, to be consistent with other calls
     */
    public static native float getMousePosY(); /*
        return ImGui::GetMousePos().y;
    */

    /**
     * Retrieve backup of mouse position at the time of opening popup we have BeginPopup() into
     */
    public static ImVec2 getMousePosOnOpeningCurrentPopup() {
        final ImVec2 value = new ImVec2();
        getMousePosOnOpeningCurrentPopup(value);
        return value;
    }

    /**
     * Retrieve backup of mouse position at the time of opening popup we have BeginPopup() into
     */
    public static native void getMousePosOnOpeningCurrentPopup(ImVec2 dstImVec2); /*
        Jni::ImVec2Cpy(env, ImGui::GetMousePosOnOpeningCurrentPopup(), dstImVec2);
    */

    /**
     * Retrieve backup of mouse position at the time of opening popup we have BeginPopup() into
     */
    public static native float getMousePosOnOpeningCurrentPopupX(); /*
        return ImGui::GetMousePosOnOpeningCurrentPopup().x;
    */

    /**
     * Retrieve backup of mouse position at the time of opening popup we have BeginPopup() into
     */
    public static native float getMousePosOnOpeningCurrentPopupY(); /*
        return ImGui::GetMousePosOnOpeningCurrentPopup().y;
    */

    /**
     * Return the delta from the initial clicking position while the mouse button is pressed or was just released.
     * This is locked and return 0.0f until the mouse moves past a distance threshold at least once. If lockThreshold {@code < -1.0f} uses io.MouseDraggingThreshold.
     */
    public static ImVec2 getMouseDragDelta() {
        final ImVec2 value = new ImVec2();
        getMouseDragDelta(value);
        return value;
    }

    /**
     * Return the delta from the initial clicking position while the mouse button is pressed or was just released.
     * This is locked and return 0.0f until the mouse moves past a distance threshold at least once. If lockThreshold {@code < -1.0f} uses io.MouseDraggingThreshold.
     */
    public static native void getMouseDragDelta(ImVec2 dstImVec2); /*
        Jni::ImVec2Cpy(env, ImGui::GetMouseDragDelta(), dstImVec2);
    */

    /**
     * Return the delta from the initial clicking position while the mouse button is pressed or was just released.
     * This is locked and return 0.0f until the mouse moves past a distance threshold at least once. If lockThreshold {@code < -1.0f} uses io.MouseDraggingThreshold.
     */
    public static native float getMouseDragDeltaX(); /*
        return ImGui::GetMouseDragDelta().x;
    */

    /**
     * Return the delta from the initial clicking position while the mouse button is pressed or was just released.
     * This is locked and return 0.0f until the mouse moves past a distance threshold at least once. If lockThreshold {@code < -1.0f} uses io.MouseDraggingThreshold.
     */
    public static native float getMouseDragDeltaY(); /*
        return ImGui::GetMouseDragDelta().y;
    */

    /**
     * Return the delta from the initial clicking position while the mouse button is pressed or was just released.
     * This is locked and return 0.0f until the mouse moves past a distance threshold at least once. If lockThreshold {@code < -1.0f} uses io.MouseDraggingThreshold.
     */
    public static ImVec2 getMouseDragDelta(final int button) {
        final ImVec2 value = new ImVec2();
        getMouseDragDelta(value, button);
        return value;
    }

    /**
     * Return the delta from the initial clicking position while the mouse button is pressed or was just released.
     * This is locked and return 0.0f until the mouse moves past a distance threshold at least once. If lockThreshold {@code < -1.0f} uses io.MouseDraggingThreshold.
     */
    public static native void getMouseDragDelta(ImVec2 dstImVec2, int button); /*
        Jni::ImVec2Cpy(env, ImGui::GetMouseDragDelta(button), dstImVec2);
    */

    /**
     * Return the delta from the initial clicking position while the mouse button is pressed or was just released.
     * This is locked and return 0.0f until the mouse moves past a distance threshold at least once. If lockThreshold {@code < -1.0f} uses io.MouseDraggingThreshold.
     */
    public static native float getMouseDragDeltaX(int button); /*
        return ImGui::GetMouseDragDelta(button).x;
    */

    /**
     * Return the delta from the initial clicking position while the mouse button is pressed or was just released.
     * This is locked and return 0.0f until the mouse moves past a distance threshold at least once. If lockThreshold {@code < -1.0f} uses io.MouseDraggingThreshold.
     */
    public static native float getMouseDragDeltaY(int button); /*
        return ImGui::GetMouseDragDelta(button).y;
    */

    /**
     * Return the delta from the initial clicking position while the mouse button is pressed or was just released.
     * This is locked and return 0.0f until the mouse moves past a distance threshold at least once. If lockThreshold {@code < -1.0f} uses io.MouseDraggingThreshold.
     */
    public static ImVec2 getMouseDragDelta(final int button, final float lockThreshold) {
        final ImVec2 value = new ImVec2();
        getMouseDragDelta(value, button, lockThreshold);
        return value;
    }

    /**
     * Return the delta from the initial clicking position while the mouse button is pressed or was just released.
     * This is locked and return 0.0f until the mouse moves past a distance threshold at least once. If lockThreshold {@code < -1.0f} uses io.MouseDraggingThreshold.
     */
    public static native void getMouseDragDelta(ImVec2 dstImVec2, int button, float lockThreshold); /*
        Jni::ImVec2Cpy(env, ImGui::GetMouseDragDelta(button, lockThreshold), dstImVec2);
    */

    /**
     * Return the delta from the initial clicking position while the mouse button is pressed or was just released.
     * This is locked and return 0.0f until the mouse moves past a distance threshold at least once. If lockThreshold {@code < -1.0f} uses io.MouseDraggingThreshold.
     */
    public static native float getMouseDragDeltaX(int button, float lockThreshold); /*
        return ImGui::GetMouseDragDelta(button, lockThreshold).x;
    */

    /**
     * Return the delta from the initial clicking position while the mouse button is pressed or was just released.
     * This is locked and return 0.0f until the mouse moves past a distance threshold at least once. If lockThreshold {@code < -1.0f} uses io.MouseDraggingThreshold.
     */
    public static native float getMouseDragDeltaY(int button, float lockThreshold); /*
        return ImGui::GetMouseDragDelta(button, lockThreshold).y;
    */

    public static native void resetMouseDragDelta(); /*
        ImGui::ResetMouseDragDelta();
    */

    public static native void resetMouseDragDelta(int button); /*
        ImGui::ResetMouseDragDelta(button);
    */

    /**
     * Get desired cursor type, reset in ImGui::NewFrame(), this is updated during the frame. valid before Render().
     * If you use software rendering by setting io.MouseDrawCursor ImGui will render those for you
     */
    public static native int getMouseCursor(); /*
        return ImGui::GetMouseCursor();
    */

    /**
     * Set desired cursor type
     */
    public static native void setMouseCursor(int type); /*
        ImGui::SetMouseCursor(type);
    */

    /**
     * Attention: misleading name! manually override io.WantCaptureMouse flag next frame (said flag is entirely left for your application to handle).
     * This is equivalent to setting "io.WantCaptureMouse = wantCaptureMouseValue;" after the next NewFrame() call.
     */
    public static native void captureMouseFromApp(); /*
        ImGui::CaptureMouseFromApp();
    */

    /**
     * Attention: misleading name! manually override io.WantCaptureMouse flag next frame (said flag is entirely left for your application to handle).
     * This is equivalent to setting "io.WantCaptureMouse = wantCaptureMouseValue;" after the next NewFrame() call.
     */
    public static native void captureMouseFromApp(boolean wantCaptureMouseValue); /*
        ImGui::CaptureMouseFromApp(wantCaptureMouseValue);
    */

    // Clipboard Utilities
    // - Also see the LogToClipboard() function to capture GUI into clipboard, or easily output text data to the clipboard.

    public static native String getClipboardText(); /*
        return env->NewStringUTF(ImGui::GetClipboardText());
    */

    public static native void setClipboardText(String text); /*
        ImGui::SetClipboardText(text);
    */

    // Settings/.Ini Utilities
    // - The disk functions are automatically called if io.IniFilename != NULL (default is "imgui.ini").
    // - Set io.IniFilename to NULL to load/save manually. Read io.WantSaveIniSettings description about handling .ini saving manually.

    /**
     * Call after CreateContext() and before the first call to NewFrame(). NewFrame() automatically calls LoadIniSettingsFromDisk(io.IniFilename).
     */
    public static native void loadIniSettingsFromDisk(String iniFilename); /*
        ImGui::LoadIniSettingsFromDisk(iniFilename);
    */

    /**
     * Call after CreateContext() and before the first call to NewFrame() to provide .ini data from your own data source.
     */
    public static native void loadIniSettingsFromMemory(String iniData); /*
        ImGui::LoadIniSettingsFromMemory(iniData);
    */

    /**
     * Call after CreateContext() and before the first call to NewFrame() to provide .ini data from your own data source.
     */
    public static native void loadIniSettingsFromMemory(String iniData, int iniSize); /*
        ImGui::LoadIniSettingsFromMemory(iniData, iniSize);
    */

    /**
     * This is automatically called (if io.IniFilename is not empty) a few seconds after any modification that should be reflected in the .ini file (and also by DestroyContext).
     */
    public static native void saveIniSettingsToDisk(String iniFilename); /*
        ImGui::SaveIniSettingsToDisk(iniFilename);
    */

    /**
     * Return a zero-terminated string with the .ini data which you can save by your own mean.
     * Call when io.WantSaveIniSettings is set, then save data by your own mean and clear io.WantSaveIniSettings.
     */
    public static native String saveIniSettingsToMemory(); /*
        return env->NewStringUTF(ImGui::SaveIniSettingsToMemory());
    */

    /**
     * Return a zero-terminated string with the .ini data which you can save by your own mean.
     * Call when io.WantSaveIniSettings is set, then save data by your own mean and clear io.WantSaveIniSettings.
     */
    public static native String saveIniSettingsToMemory(long outIniSize); /*
        return env->NewStringUTF(ImGui::SaveIniSettingsToMemory((size_t*)&outIniSize));
    */

    // (Optional) Platform/OS interface for multi-viewport support
    // Read comments around the ImGuiPlatformIO structure for more details.
    // Note: You may use GetWindowViewport() to get the current viewport of the current window.

    /**
     * Platform/renderer functions, for backend to setup + viewports list.
     */
    public static ImGuiPlatformIO getPlatformIO() {
        PLATFORM_IO.ptr = nGetPlatformIO();
        return PLATFORM_IO;
    }

    private static native long nGetPlatformIO(); /*
        return (intptr_t)&ImGui::GetPlatformIO();
    */

    /**
     * Call in main loop. Will call CreateWindow/ResizeWindow/etc. Platform functions for each secondary viewport, and DestroyWindow for each inactive viewport.
     */
    public static native void updatePlatformWindows(); /*
        ImGui::UpdatePlatformWindows();
    */

    /**
     *  Call in main loop. will call RenderWindow/SwapBuffers platform functions for each secondary viewport which doesn't have the ImGuiViewportFlags_Minimized flag set.
     *  May be reimplemented by user for custom rendering needs.
     */
    public static native void renderPlatformWindowsDefault(); /*
        ImGui::RenderPlatformWindowsDefault();
    */

    /**
     * Call DestroyWindow platform functions for all viewports.
     * Call from backend Shutdown() if you need to close platform windows before imgui shutdown.
     * Otherwise will be called by DestroyContext().
     */
    public static native void destroyPlatformWindows(); /*
        ImGui::DestroyPlatformWindows();
    */

    /**
     * This is a helper for backends.
     */
    public static ImGuiViewport findViewportByID(int imGuiID) {
        FIND_VIEWPORT.ptr = nFindViewportByID(imGuiID);
        return FIND_VIEWPORT;
    }

    private static native long nFindViewportByID(int imGuiID); /*
        return (intptr_t)ImGui::FindViewportByID(imGuiID);
    */

    /**
     * This is a helper for backends. The type platform_handle is decided by the backend (e.g. HWND, MyWindow*, GLFWwindow* etc.)
     */
    public static ImGuiViewport findViewportByPlatformHandle(long platformHandle) {
        FIND_VIEWPORT.ptr = nFindViewportByPlatformHandle(platformHandle);
        return FIND_VIEWPORT;
    }

    private static native long nFindViewportByPlatformHandle(long platformHandle); /*
        return (intptr_t)ImGui::FindViewportByPlatformHandle((void*)platformHandle);
    */
    // CUSTOM API: END
}
