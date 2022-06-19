package tool.generator.api.metadata.def.imgui

import tool.generator.api.metadata.ApiMetadata
import tool.generator.api.metadata.ApiResult

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
        method("ShowDemoWindow") { argBoolPtr("open", true) }
        method("ShowMetricsWindow") { argBoolPtr("open", true) }
        method("ShowStackToolWindow") { argBoolPtr("open", true) }
        method("ShowStyleEditor") { argStruct("imgui.ImGuiStyle", "ref", true) }
        method("ShowStyleSelector", resultBoolean()) { argStr("label") }
        method("ShowFontSelector") { argStr("label") }
        method("ShowUserGuide")
        method("GetVersion", resultString())

        // Styles
        method("StyleColorsDark") { argStruct("imgui.ImGuiStyle", "dst", true) }
        method("StyleColorsLight") { argStruct("imgui.ImGuiStyle", "dst", true) }
        method("StyleColorsClassic") { argStruct("imgui.ImGuiStyle", "dst", true) }

        // Windows
        method("Begin", resultBoolean()) {
            argStr("name")
            argBoolPtr("pOpen", true, "NULL")
            argInt("flags", true)
        }
        method("End")

        // Child Windows
        method("BeginChild", resultBoolean()) {
            argStr("strId")
            argImVec2("size", true, "ImVec2(0, 0)")
            argBool("border", true, "false")
            argInt("flags", true)
        }
        method("BeginChild", resultBoolean()) {
            argInt("id")
            argImVec2("size", true)
            argBool("border", true)
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
            argBool("collapsed")
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
            argBool("collapsed")
            argInt("cond", default = "ImGuiCond_None")
        }
        method("SetWindowCollapsed") {
            argStr("name")
            argBool("collapsed")
            argInt("cond", default = "ImGuiCond_None")
        }
        method("SetWindowFocus")
        method("SetWindowFontScale") { argFloat("scale") }
        method("SetWindowPos") {
            argStr("name")
            argImVec2("pos")
            argInt("cond", true)
        }
        method("SetWindowSize") {
            argStr("name")
            argImVec2("size")
            argInt("cond", true)
        }
        method("SetWindowFocus") {
            argStr("name")
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
        method("PushAllowKeyboardFocus") { argBool("allowKeyboardFocus") }
        method("PopAllowKeyboardFocus")
        method("PushButtonRepeat") { argBool("repeat") }
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
    }
}
