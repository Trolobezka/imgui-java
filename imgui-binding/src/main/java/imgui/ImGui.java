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

    private static WeakReference<Object> payloadRef = null;
    private static final byte[] PAYLOAD_PLACEHOLDER_DATA = new byte[1];

    /**
     * Type is a user defined string of maximum 32 characters. Strings starting with '_' are reserved for dear imgui internal types.
     * <p>
     * Method adopted for Java, so objects are used instead of raw bytes.
     * Binding stores a reference to the object in a form of a {@link WeakReference}.
     */
    public static boolean setDragDropPayload(final String dataType, final Object payload) {
        return setDragDropPayload(dataType, payload, ImGuiCond.None);
    }

    /**
     * Type is a user defined string of maximum 32 characters. Strings starting with '_' are reserved for dear imgui internal types.
     * <p>
     * Method adopted for Java, so objects are used instead of raw bytes.
     * Binding stores a reference to the object in a form of a {@link WeakReference}.
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

    public static native void colorConvertRGBtoHSV(float[] rgb, float[] hsv); /*
        ImGui::ColorConvertRGBtoHSV(rgb[0], rgb[1], rgb[2], hsv[0], hsv[1], hsv[2]);
    */

    public static native void colorConvertHSVtoRGB(float[] hsv, float[] rgb); /*
        ImGui::ColorConvertHSVtoRGB(hsv[0], hsv[1], hsv[2], rgb[0], rgb[1], rgb[2]);
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
