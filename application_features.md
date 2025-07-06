# 📱 Advanced Photo Editing Android App - Features & Free On-Device Stack

---

## 🚀 Summary

This app is a **full-featured, advanced photo editor for Android** that is:

- 100% **free**, **open-source**, using only **on-device processing**.
- Built with **Kotlin + Jetpack Compose** (single screen, bottom sheets & overlays).
- Uses **no paid SDKs or server-side processing** — everything happens on the device for privacy & speed.

---

## 🌟 Core Features List

### 🌅 Import & Export
- Import from device gallery
- Capture directly from camera
- Export as JPG, PNG, WebP
- Adjustable JPG quality & compression
- EXIF metadata control (keep/remove)

### ✂️ Crop & Rotate
- Freeform crop
- Aspect ratio presets (1:1, 4:5, 16:9)
- Rotate 90°, custom degrees
- Flip horizontally or vertically
- Grid overlays for alignment

### 🎨 Filters & Adjustments
- Artistic one-tap filters (via GPUImage shaders)
- Adjustable intensity for all filters
- Basic adjustments: brightness, contrast, saturation, warmth
- Advanced: shadows, highlights, clarity
- Histogram overlay (optional)

### 🖌️ Selective Editing & Brush
- Brush to adjust exposure/saturation locally
- Adjustable brush size, softness
- Erase brush edits selectively

### 🌈 Color Grading
- HSL sliders (Hue, Saturation, Lightness)
- Color balance for shadows/mids/highlights
- LUT import & apply (via GPUImage shader)

### 🎭 Effects & Overlays
- Vignette, grain, texture overlays
- Light leaks, bokeh effects
- Lens blur (depth-of-field look)

### 🖍️ Text, Stickers & Draw
- Add text with custom fonts, color, shadow
- Add stickers / emojis
- Layers control (bring forward / backward)
- Freehand draw with color, opacity, blend modes
- Insert shapes (arrows, rectangles)

### 🔍 Portrait & Retouch Tools
- Face detection (ML Kit)
- Blemish remover (tap to fix small spots)
- Skin smoothen, eye whitening

### 🚀 AI & Smart Enhance
- One-tap auto enhance (small local ML model)
- Scene detection (portrait, food etc. via lightweight TFLite)
- Before/after toggle for comparison

### 🖼 Background Removal
- On-device background remove using **MediaPipe Selfie Segmentation via TFLite**
- Replace with transparent background or new image

### 💾 Single Screen UX
- All edits on **one screen** via bottom sheets & Compose overlays
- Undo, redo, reset
- Compare original (tap & hold)
- Dark / light theme
- Smooth animations, haptic feedback

---

## 👥 User Stories

### 🌅 Import & Export
- As a user, I want to import photos from my gallery or camera so I can edit any picture easily.
- As a user, I want to export my edited photo in JPG, PNG or WebP formats with adjustable quality.
- As a user, I want to decide whether to keep or remove EXIF metadata for privacy.

### ✂️ Basic Edits
- As a user, I want to crop and rotate my photo freely or use standard ratios.
- As a user, I want to flip images for creative looks.

### 🎨 Filters & Adjustments
- As a user, I want to apply filters and adjust their strength instantly.
- As a user, I want to tweak brightness, contrast, saturation and warmth on the same screen and see changes live.

### 🖌️ Brush & Local Adjustments
- As a user, I want to brush on adjustments to fix small parts of my image selectively.

### 🎭 Effects
- As a user, I want to add overlays like light leaks and bokeh for creative styles.

### 📝 Text, Stickers & Draw
- As a user, I want to add text and stickers to customize my photo.
- As a user, I want to draw freehand with adjustable size & opacity.

### 🚀 AI & Enhance
- As a user, I want one-tap auto-enhance so my photo looks better instantly.
- As a user, I want to toggle before/after view.

### 🖼 Background Remove
- As a user, I want to remove the background of a photo on my device without internet so I can place it on new backgrounds.

### 💾 Single Screen UX
- As a user, I want all edits on a single screen so I don’t lose my workflow.
- As a user, I want undo/redo/reset for safe experimenting.

---

## 🔥 Feature Matrix with Priorities

| Feature                            | Priority | Notes |
|------------------------------------|----------|-------|
| Import gallery & camera            | High     | Core workflow **(Implemented)** |
| Export JPG/PNG/WebP, EXIF control  | High     | Privacy & formats |
| Crop, rotate, flip                 | High     | Basic edit **(Implemented)** |
| Filters & intensity adjust         | High     | Artistic looks |
| Basic adjustments sliders          | High     | Essential editing |
| One-tap auto enhance (TFLite)       | High     | Smart quick fix |
| Single screen workflow             | High     | Main UX principle **(Implemented)** |
| Undo/redo/reset, compare original  | High     | User control |
| Advanced adjust (shadows, clarity) | Medium   | Next level edit |
| Brush selective editing            | Medium   | Targeted fixes |
| Text, stickers, draw               | Medium   | Decoration |
| Dark/light theme                   | Medium   | Accessibility |
| Background remove (TFLite MediaPipe)| Medium   | Smart local edit |
| Save custom presets                | Medium   | Faster repeats |
| HSL, LUT grading                   | Low      | Pro-grade color |
| Overlays: leaks, bokeh             | Low      | Artistic effects |
| Retouch: blemish, smooth skin      | Low      | Portrait fixes |
| Shapes, blend modes                | Low      | Graphics design |
| Haptic & animation polish          | Low      | UX finishing touch |

---

✅ **Completely free stack using:**
- GPUImage, uCrop, Coil, Compose Canvas
- ML Kit & TFLite for smart features (no server, no cost, 100% local).

🎯 *Designed to be private, offline, and fully open-source.*