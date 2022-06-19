package tool.generator.api.metadata.def.imgui

import tool.generator.api.metadata.ApiMetadata

class ImGui : ApiMetadata() {
    init {
        receiver = "ImGui::"
        static = true

        // Context creation and access
        method("CreateContext", resultStruct("imgui.internal.ImGuiContext")) {
            argStruct("imgui.ImFontAtlas", "sharedFontAtlas", true)
        }
        method("DestroyContext") {
            argStruct("imgui.internal.ImGuiContext", "ctx", true)
        }
        method("GetCurrentContext", resultStruct("imgui.internal.ImGuiContext", static = true))
        method("SetCurrentContext") {
            argStruct("imgui.internal.ImGuiContext", "ctx")
        }

        // Main
        method("GetIO", resultStruct("imgui.ImGuiIO", static = true, isRef = true))
        method("GetStyle", resultStruct("imgui.ImGuiStyle", static = true, isRef = true))
        method("NewFrame")
        method("EndFrame")
        method("Render")
        method("GetDrawData", resultStruct("imgui.ImDrawData", static = true))

        // Demo, Debug, Information
        method("ShowDemoWindow") { argBooleanPtr("open", true) }
        method("ShowMetricsWindow") { argBooleanPtr("open", true) }
        method("ShowStackToolWindow") { argBooleanPtr("open", true) }
        method("ShowStyleEditor") { argStruct("imgui.ImGuiStyle", "ref", true) }
        method("ShowStyleSelector", resultBoolean()) { argString("label") }
        method("ShowFontSelector") { argString("label") }
        method("ShowUserGuide")
        method("GetVersion", resultString())

        // Styles
        method("StyleColorsDark") { argStruct("imgui.ImGuiStyle", "dst", true) }
        method("StyleColorsLight") { argStruct("imgui.ImGuiStyle", "dst", true) }
        method("StyleColorsClassic") { argStruct("imgui.ImGuiStyle", "dst", true) }

        // Windows
        method("Begin", resultBoolean()) {
            argString("name")
            argBooleanPtr("pOpen", true, "NULL")
            argInt("flags", true)
        }
        method("End")

        // Child Windows
        method("BeginChild", resultBoolean()) {
            argString("strId")
            argImVec2("size", true, "ImVec2(0, 0)")
            argBoolean("border", true, "false")
            argInt("flags", true)
        }
        method("BeginChild", resultBoolean()) {
            argInt("id")
            argImVec2("size", true)
            argBoolean("border", true)
            argInt("flags", true)
        }
        method("EndChild")

        // Windows Utilities
        method("IsWindowAppearing", resultBoolean())
        method("IsWindowCollapsed", resultBoolean())
        method("IsWindowFocused", resultBoolean()) { argInt("flags", true) }
        method("IsWindowHovered", resultBoolean()) { argInt("flags", true) }
        method("GetWindowDrawList", resultStruct("imgui.ImDrawList"))
        method("GetWindowDpiScale", resultFloat())
        method("GetWindowPos", resultImVec2())
        method("GetWindowSize", resultImVec2())
        method("GetWindowWidth", resultFloat())
        method("GetWindowHeight", resultFloat())
        method("GetWindowViewport", resultStruct("imgui.ImGuiViewport"))

        // Window manipulation
        method("SetNextWindowPos") {
            argImVec2("pos")
            argInt("cond", true)
            argImVec2("pivot", true)
        }
        method("SetNextWindowSize") {
            argImVec2("size")
            argInt("cond", true)
        }
        method("SetNextWindowSizeConstraints") {
            argImVec2("sizeMin")
            argImVec2("sizeMax")
        }
        method("SetNextWindowContentSize") { argImVec2("size") }
        method("SetNextWindowCollapsed") {
            argBoolean("collapsed")
            argInt("cond", true)
        }
        method("SetNextWindowFocus")
        method("SetNextWindowBgAlpha") { argFloat("alpha") }
        method("SetNextWindowViewport") { argInt("viewportId") }
        method("SetWindowPos") {
            argImVec2("pos")
            argInt("cond", true)
        }
        method("SetWindowSize") {
            argImVec2("size")
            argInt("cond", true)
        }
        method("SetWindowCollapsed") {
            argBoolean("collapsed")
            argInt("cond", default = "ImGuiCond_None")
        }
        method("SetWindowCollapsed") {
            argString("name")
            argBoolean("collapsed")
            argInt("cond", default = "ImGuiCond_None")
        }
        method("SetWindowFocus")
        method("SetWindowFontScale") { argFloat("scale") }
        method("SetWindowPos") {
            argString("name")
            argImVec2("pos")
            argInt("cond", true)
        }
        method("SetWindowSize") {
            argString("name")
            argImVec2("size")
            argInt("cond", true)
        }
        method("SetWindowFocus") {
            argString("name")
        }

        // Content region
        method("GetContentRegionAvail", resultImVec2())
        method("GetContentRegionMax", resultImVec2())
        method("GetWindowContentRegionMin", resultImVec2())
        method("GetWindowContentRegionMax", resultImVec2())

        // Windows Scrolling
        method("GetScrollX", resultFloat())
        method("GetScrollY", resultFloat())
        method("SetScrollX") { argFloat("scrollX") }
        method("SetScrollY") { argFloat("scrollY") }
        method("GetScrollMaxX", resultFloat())
        method("GetScrollMaxY", resultFloat())
        method("SetScrollHereX") { argFloat("centerRatioX", true) }
        method("SetScrollHereY") { argFloat("centerRatioY", true) }
        method("SetScrollFromPosX") {
            argFloat("localX")
            argFloat("centerRatioX", true)
        }
        method("SetScrollFromPosY") {
            argFloat("localY")
            argFloat("centerRatioY", true)
        }

        // Parameters stacks (shared)
        method("PushFont") { argStruct("imgui.ImFont", "font") }
        method("PopFont")
        method("PushStyleColor") {
            argInt("idx")
            argInt("col")
        }
        method("PushStyleColor") {
            argInt("idx")
            argImVec4("col")
        }
        method("PopStyleColor") { argInt("count", true) }
        method("PushStyleVar") {
            argInt("idx")
            argInt("col")
        }
        method("PushStyleVar") {
            argInt("idx")
            argImVec2("val")
        }
        method("PopStyleVar") { argInt("count", true) }
        method("PushAllowKeyboardFocus") { argBoolean("allowKeyboardFocus") }
        method("PopAllowKeyboardFocus")
        method("PushButtonRepeat") { argBoolean("repeat") }
        method("PopButtonRepeat")

        // Parameters stacks (current window)
        method("PushItemWidth") { argFloat("itemWidth") }
        method("PopItemWidth")
        method("SetNextItemWidth") { argFloat("itemWidth") }
        method("CalcItemWidth", resultFloat())
        method("PushTextWrapPos") { argFloat("wrapLocalPosX", true) }
        method("PopTextWrapPos")

        // Style read access
        method("GetFont", resultStruct("imgui.ImFont"))
        method("GetFontSize", resultFloat())
        method("GetFontTexUvWhitePixel", resultImVec2())
        method("GetColorU32", resultInt()) {
            argInt("idx")
            argFloat("alphaMul")
        }
        method("GetColorU32", resultInt()) { argImVec4("col") }
        method("GetStyleColorVec4", resultImVec4()) { argInt("idx") }

        // Cursor / Layout
        method("Separator")
        method("SameLine") {
            argFloat("offsetFromStart", optional = true)
            argFloat("spacing", optional = true)
        }
        method("NewLine")
        method("Spacing")
        method("Dummy") { argImVec2("size") }
        method("Indent") { argFloat("indentW", optional = true) }
        method("Unindent") { argFloat("indentW", optional = true) }
        method("BeginGroup")
        method("EndGroup")
        method("GetCursorPos", resultImVec2())
        method("SetCursorPos") { argImVec2("localPos") }
        method("SetCursorPosX") { argFloat("localX") }
        method("SetCursorPosY") { argFloat("localY") }
        method("GetCursorStartPos", resultImVec2())
        method("GetCursorScreenPos", resultImVec2())
        method("SetCursorScreenPos") { argImVec2("localPos") }
        method("AlignTextToFramePadding")
        method("GetTextLineHeight", resultFloat())
        method("GetTextLineHeightWithSpacing", resultFloat())
        method("GetFrameHeight", resultFloat())
        method("GetFrameHeightWithSpacing", resultFloat())

        // ID stack/scopes
        method("PushID") { argString("id") }
        method("PushID") { argInt("id") }
        method("PopID")
        method("GetID", resultInt()) { argString("id") }

        // Widgets: Text
        method("TextUnformatted") { argString("text") }
        method("Text") {
            argString("text")
            argNull()
        }
        method("TextColored") {
            argImVec4("col")
            argString("text")
            argNull()
        }
        method("TextDisabled") {
            argString("text")
            argNull()
        }
        method("TextWrapped") {
            argString("text")
            argNull()
        }
        method("LabelText") {
            argString("text")
            argNull()
        }
        method("BulletText") {
            argString("text")
            argNull()
        }

        // Widgets: Main
        method("Button", resultBoolean()) {
            argString("label")
            argImVec2("size", optional = true)
        }
        method("SmallButton", resultBoolean()) { argString("label") }
        method("InvisibleButton", resultBoolean()) {
            argString("id")
            argImVec2("size")
            argInt("flags", optional = true)
        }
        method("ArrowButton", resultBoolean()) {
            argString("id")
            argInt("dir")
        }
        method("Image") {
            argIntCast("userTextureId", "(ImTextureID)(intptr_t)")
            argImVec2("size")
            argImVec2("uv0", optional = true)
            argImVec2("uv1", optional = true)
            argImVec4("tintCol", optional = true)
            argImVec4("borderCol", optional = true)
        }
        method("ImageButton", resultBoolean()) {
            argIntCast("userTextureId", "(ImTextureID)(intptr_t)")
            argImVec2("size")
            argImVec2("uv0", optional = true)
            argImVec2("uv1", optional = true)
            argFloat("framePadding", optional = true)
            argImVec4("bgCol", optional = true)
            argImVec4("tintCol", optional = true)
        }
        method("Checkbox", resultBoolean()) {
            argString("label")
            argBooleanPtr("v")
        }
        method("CheckboxFlags", resultBoolean()) {
            argString("label")
            argIntPtr("flags")
            argInt("flagsValue")
        }
        method("RadioButton", resultBoolean()) {
            argString("label")
            argBoolean("active")
        }
        method("RadioButton", resultBoolean()) {
            argString("label")
            argIntPtr("v")
            argInt("vButton")
        }
        method("ProgressBar") {
            argFloat("fraction")
            argImVec2("sizeArg", optional = true, default = "ImVec2(-FLT_MIN, 0)")
            argString("overlay", optional = true)
        }
        method("Bullet")
    }
}
