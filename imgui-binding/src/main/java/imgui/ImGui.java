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
