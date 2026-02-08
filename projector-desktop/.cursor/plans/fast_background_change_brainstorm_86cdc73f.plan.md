---
name: Fast background change brainstorm
overview: Brainstorm of options to let users change the projection screen background quickly, centered on a right-side closable panel that can host Preview, quick settings, background selection, and more (e.g. GIFs).
todos: []
isProject: false
---

# Fast background change – brainstorm

**Context (from codebase):**

- **Projection screen background** is configured in [ProjectionScreenSettings.fxml](src/main/resources/view/ProjectionScreenSettings.fxml) (ColorPicker + image path) and applied in [ProjectionScreenController.setBackgroundBySettings()](src/main/java/projector/controller/ProjectionScreenController.java). The main toolbar in [MainView.fxml](src/main/resources/view/MainView.fxml) already has **Black** (blank), **Clear**, **Preview**, **Lock** — no quick background switcher yet.
- **Blank** is a temporary black overlay; it does not change the stored background. Un-blanking shows the same configured background again.

**Assumption:** “Background” here = **projection screen** background (color or image). If you meant the main app window theme (light/dark CSS), the same ideas can be adapted for theme switching.

---

## Right-side closable panel (chosen direction)

**Idea:** A **closable panel on the right** of the main content that acts as a single place for "live" and quick controls. This keeps the toolbar minimal and groups related features in one area.

**Layout:** [MainView.fxml](src/main/resources/view/MainView.fxml) uses a `BorderPane` with `center` = AnchorPane (TabPane + top-right toolbar). Add a **right** region that:

- Has a fixed or resizable width (e.g. 280–360 px).
- Can be **closed** (collapsed) so the center expands to full width; when closed, a way to reopen (e.g. toolbar button, View menu, or a thin "tab" on the right edge).
- Optionally **persist open/closed state** in [Settings](src/main/java/projector/application/Settings.java) so it restores on next launch.

**Possible contents of the panel (sections, from top to bottom or in tabs/accordion):**

- **Preview** — Show a live mini version of the projection: either embed a small projection view (same content as main) or a button that opens the current floating preview window. Embedded = always visible when panel is open.
- **Quick settings** — Frequently used toggles (e.g. Blank, Lock, progress line, next section) without opening full Projection Screen Settings; can reuse parts of [ProjectionScreenSettingsController](src/main/java/projector/controller/ProjectionScreenSettingsController.java).
- **Background** — Fast color/picture selection: color presets (chips or dropdown) + image presets or "Browse…", applying via existing `ProjectionScreenSettings` + `ProjectionScreenController.setBackgroundBySettings()`.
- **GIFs / media** — Quick insert or overlay (e.g. list of GIFs to show on projection, or "current context" for countdown, announcements); scope can be defined later.

**Implementation sketch:**

- New FXML for the panel content (e.g. `RightPanel.fxml`) with a dedicated controller; panel root could be a `VBox` or `ScrollPane` with sections.
- MainView: add `<right><fx:include source="RightPanel.fxml" fx:id="rightPanel"/></right>` and bind visibility/managed to a "panel open" property; when closed, set right to null or node's visible/managed to false so the center grows.
- Toolbar: add a toggle button (e.g. "Panel" or chevron icon) that shows/hides the panel; optionally remove or repurpose the standalone **Preview** button if preview moves into the panel.
- Styling: use existing [application.css](src/main/resources/view/application.css) / [applicationDark.css](src/main/resources/view/applicationDark.css); optional separator between center and panel.

This gives one place for **Preview**, **quick settings**, **background (color/picture)**, and **GIFs** without crowding the top toolbar, and leaves room for more "current context" items later.

---

## 1. Toolbar presets (quick colors / images) — can live inside the panel

**Idea:** Add a small control (in the panel or toolbar) that switches background **without** opening the full projection screen settings.

- **Color presets:** Dropdown or a row of color chips (e.g. Black, White, Dark blue, Custom) that apply to the **active / main** projection screen. One click applies and calls the same path as settings (e.g. `ProjectionScreenSettings.setBackgroundColor` + `ProjectionScreenController.setBackGroundColor2()`).
- **Image presets:** Same idea with 3–5 “favorite” background images (paths stored in Settings or a small “background presets” model). Dropdown or menu: “Background image 1”, “Background image 2”, “Color only”, etc.
- **Pros:** Very fast, visible, no dialog. **Cons:** Toolbar can get crowded; need to define “active” screen when multiple projection screens exist.

**Relevant code:** [MyController](src/main/java/projector/controller/MyController.java) (toolbar actions), [ProjectionScreensUtil.getProjectionScreenHolders()](src/main/java/projector/controller/util/ProjectionScreensUtil.java), [ProjectionScreenController.setBackGroundColor2 / setBackgroundBySettings](src/main/java/projector/controller/ProjectionScreenController.java).

---

## 2. Right‑click or “background” button → popover / menu

**Idea:** One toolbar button (e.g. “Background” or a palette icon) that opens a **compact popover** (or context menu) instead of the full settings window.

- **Contents:** Small color palette (e.g. 8–12 colors) + optional “Image…” and “Settings…” that open the full dialog.
- **Pros:** Single entry point, keeps toolbar clean, still fast. **Cons:** One extra click vs. presets on the bar.

---

## 3. Keyboard shortcuts

**Idea:** Shortcuts to cycle or set background (e.g. “Next background preset”, “Black background”, “White background”).

- **Implementation:** Add key handlers (e.g. in [MyController](src/main/java/projector/controller/MyController.java) or where [ProjectionScreenController](src/main/java/projector/controller/ProjectionScreenController.java) already handles keys). Map to a small set of preset colors or “next/previous” over a preset list.
- **Pros:** Very fast for power users; no UI space. **Cons:** Discoverability; need to document (and optionally show in menu/tooltip).

---

## 4. “Favorites” or “Recent backgrounds” in settings

**Idea:** In [ProjectionScreenSettingsController](src/main/java/projector/controller/ProjectionScreenSettingsController.java) / Settings UI, add a “Favorites” or “Recent” list (colors + image paths). Then expose those same favorites in the toolbar or popover (sections 1–2). So “fast change” = choosing from the same list you maintain in settings.

- **Pros:** User-defined; no fixed presets. **Cons:** Requires a small data model and persistence (e.g. in [Settings](src/main/java/projector/application/Settings.java) or projection screen settings).

---

## 5. Separate “quick background” vs “default background”

**Idea:** “Quick” choices (e.g. Black, White) apply only for the **current session** (or until “Restore default”); the default background in Projection Screen Settings stays unchanged. So you can temporarily go to black/white for a moment, then restore.

- **Pros:** Safe, non-destructive. **Cons:** Two concepts (quick vs default); need a clear “Restore default” action.

---

## 6. Integrate with “Blank” behavior (optional)

**Idea:** When user clicks **Black**, optionally treat it as “blank with black background” vs “blank but keep current background when un-blanking”. Or add a second control: “Blank (black)” vs “Blank (current background)”. So “change background fast” could mean “choose what you see when blanked” (e.g. black vs dark blue vs logo image).

- **Pros:** Reuses the existing Blank flow. **Cons:** Might confuse “blank” (hide content) with “change background”; needs clear labels.

---

## 7. Multi‑screen behavior

**Idea:** When there are multiple projection screens ([ProjectionScreensUtil.getProjectionScreenHolders()](src/main/java/projector/controller/util/ProjectionScreensUtil.java)), define behavior for “change background fast”:

- **Option A:** Apply to the “main” or “primary” projection screen only (e.g. first in list or a designated primary).
- **Option B:** Apply to **all** projection screens (so they stay in sync).
- **Option C:** Show a tiny dropdown “Apply to: Main / Screen 2 / All” in the popover or toolbar.

---

## Summary table (conceptual)


| Approach              | Speed     | Toolbar space | Flexibility       | Best for                   |
| --------------------- | --------- | ------------- | ----------------- | -------------------------- |
| Toolbar color chips   | 1 click   | Medium        | Fixed presets     | Few preferred colors       |
| Toolbar dropdown      | 2 clicks  | Small         | Presets/favorites | Many options, compact UI   |
| Popover / menu        | 2 clicks  | One button    | Presets + custom  | Balance of speed and space |
| Keyboard shortcuts    | Instant   | None          | Preset list       | Power users                |
| Favorites in settings | 1–2 click | Depends       | User-defined      | Personalized quick change  |


---

## Suggested next steps (right-panel direction)

1. **Implement the right panel shell:** Add `BorderPane` right region in MainView, new `RightPanel.fxml` + controller, toggle button to show/hide, optional persistence of open/closed state in Settings.
2. **Panel sections (order/scope):** e.g. “Toolbar dropdown with 5 color presets + 3 image slots” or “One Background button → popover with colors + link to Settings”.
3. **Scope for background/quick settings:** One main projection screen only, or multiple (main vs all vs chooser); same as in section 7 above.
4. **Data:** For background presets/favorites: add a small model (e.g. list of colors + paths) and persist in [Settings](src/main/java/projector/application/Settings.java) or next to [ProjectionScreenSettings](src/main/java/projector/application/ProjectionScreenSettings.java).
5. **Preview in panel:** Choose embedded mini projection view vs "Open preview window" button; if embedded, reuse or mirror projection content in a small view (may require refactor of how projection content is rendered).

Once the panel shell and first section (e.g. background) are scoped, this can be turned into a step-by-step implementation plan.