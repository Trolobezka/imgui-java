package tool.generator.api.metadata.def.imgui

import tool.generator.api.metadata.ApiMetadata

class ImGui : ApiMetadata() {
    init {
        receiver = "ImGui::"
        static = true

        // Context creation and access
        method("CreateContext", resultStruct("imgui.internal.ImGuiContext")) {
            argStruct("imgui.ImFontAtlas", "sharedFontAtlas", optional = true)
        }
        method("DestroyContext") {
            argStruct("imgui.internal.ImGuiContext", "ctx", optional = true)
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
        method("ShowDemoWindow") { argBooleanPtr("open", optional = true) }
        method("ShowMetricsWindow") { argBooleanPtr("open", optional = true) }
        method("ShowStackToolWindow") { argBooleanPtr("open", optional = true) }
        method("ShowStyleEditor") { argStruct("imgui.ImGuiStyle", "ref", optional = true) }
        method("ShowStyleSelector", resultBoolean()) { argString("label") }
        method("ShowFontSelector") { argString("label") }
        method("ShowUserGuide")
        method("GetVersion", resultString())

        // Styles
        method("StyleColorsDark") { argStruct("imgui.ImGuiStyle", "dst", optional = true) }
        method("StyleColorsLight") { argStruct("imgui.ImGuiStyle", "dst", optional = true) }
        method("StyleColorsClassic") { argStruct("imgui.ImGuiStyle", "dst", optional = true) }

        // Windows
        method("Begin", resultBoolean()) {
            argString("name")
            argBooleanPtr("pOpen", optional = true, default = "NULL")
            argInt("flags", optional = true)
        }
        method("End")

        // Child Windows
        method("BeginChild", resultBoolean()) {
            argString("strId")
            argImVec2("size", optional = true, default = "ImVec2(0, 0)")
            argBoolean("border", optional = true, default = "false")
            argInt("flags", optional = true)
        }
        method("BeginChild", resultBoolean()) {
            argInt("id")
            argImVec2("size", optional = true)
            argBoolean("border", optional = true)
            argInt("flags", optional = true)
        }
        method("EndChild")

        // Windows Utilities
        method("IsWindowAppearing", resultBoolean())
        method("IsWindowCollapsed", resultBoolean())
        method("IsWindowFocused", resultBoolean()) { argInt("flags", optional = true) }
        method("IsWindowHovered", resultBoolean()) { argInt("flags", optional = true) }
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
            argInt("cond", optional = true)
            argImVec2("pivot", optional = true)
        }
        method("SetNextWindowSize") {
            argImVec2("size")
            argInt("cond", optional = true)
        }
        method("SetNextWindowSizeConstraints") {
            argImVec2("sizeMin")
            argImVec2("sizeMax")
        }
        method("SetNextWindowContentSize") { argImVec2("size") }
        method("SetNextWindowCollapsed") {
            argBoolean("collapsed")
            argInt("cond", optional = true)
        }
        method("SetNextWindowFocus")
        method("SetNextWindowBgAlpha") { argFloat("alpha") }
        method("SetNextWindowViewport") { argInt("viewportId") }
        method("SetWindowPos") {
            argImVec2("pos")
            argInt("cond", optional = true)
        }
        method("SetWindowSize") {
            argImVec2("size")
            argInt("cond", optional = true)
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
            argInt("cond", optional = true)
        }
        method("SetWindowSize") {
            argString("name")
            argImVec2("size")
            argInt("cond", optional = true)
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
        method("SetScrollHereX") { argFloat("centerRatioX", optional = true) }
        method("SetScrollHereY") { argFloat("centerRatioY", optional = true) }
        method("SetScrollFromPosX") {
            argFloat("localX")
            argFloat("centerRatioX", optional = true)
        }
        method("SetScrollFromPosY") {
            argFloat("localY")
            argFloat("centerRatioY", optional = true)
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
        method("PopStyleColor") { argInt("count", optional = true) }
        method("PushStyleVar") {
            argInt("idx")
            argInt("col")
        }
        method("PushStyleVar") {
            argInt("idx")
            argImVec2("val")
        }
        method("PopStyleVar") { argInt("count", optional = true) }
        method("PushAllowKeyboardFocus") { argBoolean("allowKeyboardFocus") }
        method("PopAllowKeyboardFocus")
        method("PushButtonRepeat") { argBoolean("repeat") }
        method("PopButtonRepeat")

        // Parameters stacks (current window)
        method("PushItemWidth") { argFloat("itemWidth") }
        method("PopItemWidth")
        method("SetNextItemWidth") { argFloat("itemWidth") }
        method("CalcItemWidth", resultFloat())
        method("PushTextWrapPos") { argFloat("wrapLocalPosX", optional = true) }
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
            argCast(argIntRaw("userTextureId"), "(ImTextureID)(intptr_t)")
            argImVec2("size")
            argImVec2("uv0", optional = true)
            argImVec2("uv1", optional = true)
            argImVec4("tintCol", optional = true)
            argImVec4("borderCol", optional = true)
        }
        method("ImageButton", resultBoolean()) {
            argCast(argIntRaw("userTextureId"), "(ImTextureID)(intptr_t)")
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

        // Widgets: Combo Box
        method("BeginCombo", resultBoolean()) {
            argString("label")
            argString("previewValue")
            argInt("flags", optional = true)
        }
        method("EndCombo")
        method("Combo", resultBoolean()) {
            argString("label")
            argIntPtr("currentItem")
            argString("itemsSeparatedByZeros")
            argInt("popupMaxHeightInItems", optional = true)
        }

        // Widgets: Drag Sliders
        method("DragFloat", resultBoolean()) {
            argString("label")
            argFloatPtr("value")
            argFloat("vSpeed", optional = true)
            argFloat("vMin", optional = true)
            argFloat("vMax", optional = true)
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("DragFloat", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argFloat("vSpeed", optional = true)
            argFloat("vMin", optional = true)
            argFloat("vMax", optional = true)
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("DragFloat2", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argFloat("vSpeed", optional = true)
            argFloat("vMin", optional = true)
            argFloat("vMax", optional = true)
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("DragFloat3", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argFloat("vSpeed", optional = true)
            argFloat("vMin", optional = true)
            argFloat("vMax", optional = true)
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("DragFloat4", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argFloat("vSpeed", optional = true)
            argFloat("vMin", optional = true)
            argFloat("vMax", optional = true)
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("DragFloatRange2", resultBoolean()) {
            argString("label")
            argFloatPtr("vCurrentMin")
            argFloatPtr("vCurrentMax")
            argFloat("vSpeed", optional = true)
            argFloat("vMin", optional = true)
            argFloat("vMax", optional = true)
            argString("format", optional = true)
            argString("formatMax", optional = true)
            argInt("flags", optional = true)
        }
        method("DragFloatRange2", resultBoolean()) {
            argString("label")
            argFloatArr("vCurrentMin")
            argFloatArr("vCurrentMax")
            argFloat("vSpeed", optional = true)
            argFloat("vMin", optional = true)
            argFloat("vMax", optional = true)
            argString("format", optional = true)
            argString("formatMax", optional = true)
            argInt("flags", optional = true)
        }
        method("DragInt", resultBoolean()) {
            argString("label")
            argIntPtr("value")
            argFloat("vSpeed", optional = true)
            argInt("vMin", optional = true)
            argInt("vMax", optional = true)
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("DragInt", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argFloat("vSpeed", optional = true)
            argInt("vMin", optional = true)
            argInt("vMax", optional = true)
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("DragInt2", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argFloat("vSpeed", optional = true)
            argInt("vMin", optional = true)
            argInt("vMax", optional = true)
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("DragInt3", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argFloat("vSpeed", optional = true)
            argInt("vMin", optional = true)
            argInt("vMax", optional = true)
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("DragInt4", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argFloat("vSpeed", optional = true)
            argInt("vMin", optional = true)
            argInt("vMax", optional = true)
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("DragIntRange2", resultBoolean()) {
            argString("label")
            argIntPtr("vCurrentMin")
            argIntPtr("vCurrentMax")
            argFloat("vSpeed", optional = true)
            argInt("vMin", optional = true)
            argInt("vMax", optional = true)
            argString("format", optional = true)
            argString("formatMax", optional = true)
            argInt("flags", optional = true)
        }
        method("DragIntRange2", resultBoolean()) {
            argString("label")
            argIntArr("vCurrentMin")
            argIntArr("vCurrentMax")
            argFloat("vSpeed", optional = true)
            argInt("vMin", optional = true)
            argInt("vMax", optional = true)
            argString("format", optional = true)
            argString("formatMax", optional = true)
            argInt("flags", optional = true)
        }
        method("DragScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_Float")
            argFloatPtr("pData")
            argFloat("vSpeed", optional = true)
            argCast(argFloatRaw("pMin", optional = true), "&")
            argCast(argFloatRaw("pMax", optional = true), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("DragScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_S16")
            argShortPtr("pData")
            argFloat("vSpeed", optional = true)
            argCast(argShortRaw("pMin", optional = true), "&")
            argCast(argShortRaw("pMax", optional = true), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("DragScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_S32")
            argIntPtr("pData")
            argFloat("vSpeed", optional = true)
            argCast(argIntRaw("pMin", optional = true), "&")
            argCast(argIntRaw("pMax", optional = true), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("DragScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_S64")
            argLongPtr("pData")
            argFloat("vSpeed", optional = true)
            argCast(argLongRaw("pMin", optional = true), "&")
            argCast(argLongRaw("pMax", optional = true), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }

        // Widgets: Regular Sliders
        method("SliderFloat", resultBoolean()) {
            argString("label")
            argFloatPtr("value")
            argFloat("vMin")
            argFloat("vMax")
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("SliderFloat", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argFloat("vMin")
            argFloat("vMax")
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("SliderFloat2", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argFloat("vMin")
            argFloat("vMax")
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("SliderFloat3", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argFloat("vMin")
            argFloat("vMax")
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("SliderFloat4", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argFloat("vMin")
            argFloat("vMax")
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("SliderAngle", resultBoolean()) {
            argString("label")
            argFloatPtr("vRad")
            argFloat("vDegreesMin")
            argFloat("vDegreesMax")
            argString("format", optional = true, default = "\"%.0f deg\"")
            argInt("flags", optional = true)
        }
        method("SliderInt", resultBoolean()) {
            argString("label")
            argIntPtr("value")
            argInt("vMin")
            argInt("vMax")
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("SliderInt", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argInt("vMin")
            argInt("vMax")
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("SliderInt2", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argInt("vMin")
            argInt("vMax")
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("SliderInt3", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argInt("vMin")
            argInt("vMax")
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("SliderInt4", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argInt("vMin")
            argInt("vMax")
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("SliderScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_Float")
            argFloatPtr("pData")
            argCast(argFloatRaw("pMin"), "&")
            argCast(argFloatRaw("pMax"), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("SliderScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_S16")
            argShortPtr("pData")
            argCast(argShortRaw("pMin"), "&")
            argCast(argShortRaw("pMax"), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("SliderScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_S32")
            argIntPtr("pData")
            argCast(argIntRaw("pMin"), "&")
            argCast(argIntRaw("pMax"), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("SliderScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_S64")
            argLongPtr("pData")
            argCast(argLongRaw("pMin"), "&")
            argCast(argLongRaw("pMax"), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("VSliderFloat", resultBoolean()) {
            argString("label")
            argImVec2("size")
            argFloatPtr("value")
            argFloat("vMin")
            argFloat("vMax")
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("VSliderInt", resultBoolean()) {
            argString("label")
            argImVec2("size")
            argIntPtr("value")
            argInt("vMin")
            argInt("vMax")
            argString("format", optional = true, default = "\"%d\"")
            argInt("flags", optional = true)
        }
        method("VSliderScalar", resultBoolean()) {
            argString("label")
            argImVec2("size")
            argDefault("ImGuiDataType_Float")
            argFloatPtr("pData")
            argCast(argFloatRaw("pMin"), "&")
            argCast(argFloatRaw("pMax"), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("VSliderScalar", resultBoolean()) {
            argString("label")
            argImVec2("size")
            argDefault("ImGuiDataType_S16")
            argShortPtr("pData")
            argCast(argFloatRaw("pMin"), "&")
            argCast(argFloatRaw("pMax"), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("VSliderScalar", resultBoolean()) {
            argString("label")
            argImVec2("size")
            argDefault("ImGuiDataType_S32")
            argIntPtr("pData")
            argCast(argFloatRaw("pMin"), "&")
            argCast(argFloatRaw("pMax"), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("VSliderScalar", resultBoolean()) {
            argString("label")
            argImVec2("size")
            argDefault("ImGuiDataType_S64")
            argLongPtr("pData")
            argCast(argFloatRaw("pMin"), "&")
            argCast(argFloatRaw("pMax"), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }

        // Widgets: Input with Keyboard
        method("InputFloat", resultBoolean()) {
            argString("label")
            argFloatPtr("value")
            argFloat("step", optional = true)
            argFloat("stepFast", optional = true)
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("InputFloat", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argFloat("step", optional = true)
            argFloat("stepFast", optional = true)
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("InputFloat2", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("InputFloat3", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("InputFloat4", resultBoolean()) {
            argString("label")
            argFloatArr("value")
            argString("format", optional = true, default = "\"%.3f\"")
            argInt("flags", optional = true)
        }
        method("InputInt", resultBoolean()) {
            argString("label")
            argIntPtr("value")
            argInt("step", optional = true)
            argInt("stepFast", optional = true)
            argInt("flags", optional = true)
        }
        method("InputInt", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argInt("step", optional = true)
            argInt("stepFast", optional = true)
            argInt("flags", optional = true)
        }
        method("InputInt2", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argInt("flags", optional = true)
        }
        method("InputInt3", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argInt("flags", optional = true)
        }
        method("InputInt4", resultBoolean()) {
            argString("label")
            argIntArr("value")
            argInt("flags", optional = true)
        }
        method("InputDouble", resultBoolean()) {
            argString("label")
            argDoublePtr("value")
            argDouble("step")
            argDouble("stepFast")
            argString("format", optional = true, default = "\"%.6f\"")
            argInt("flags", optional = true)
        }
        method("InputDouble", resultBoolean()) {
            argString("label")
            argDoubleArr("value")
            argDouble("step")
            argDouble("stepFast")
            argString("format", optional = true, default = "\"%.6f\"")
            argInt("flags", optional = true)
        }
        method("InputScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_Float")
            argFloatPtr("pData")
            argCast(argFloatRaw("pStep", optional = true), "&")
            argCast(argFloatRaw("pStepFast", optional = true), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("InputScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_S16")
            argShortPtr("pData")
            argCast(argFloatRaw("pStep", optional = true), "&")
            argCast(argFloatRaw("pStepFast", optional = true), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("InputScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_S32")
            argIntPtr("pData")
            argCast(argFloatRaw("pStep", optional = true), "&")
            argCast(argFloatRaw("pStepFast", optional = true), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
        method("InputScalar", resultBoolean()) {
            argString("label")
            argDefault("ImGuiDataType_S64")
            argLongPtr("pData")
            argCast(argFloatRaw("pStep", optional = true), "&")
            argCast(argFloatRaw("pStepFast", optional = true), "&")
            argString("format", optional = true)
            argInt("flags", optional = true)
        }
    }
}
