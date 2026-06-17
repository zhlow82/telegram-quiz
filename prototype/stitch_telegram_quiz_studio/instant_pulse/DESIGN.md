---
name: Instant Pulse
colors:
  surface: '#f9f9fa'
  surface-dim: '#dadadb'
  surface-bright: '#f9f9fa'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f3f3f4'
  surface-container: '#eeeeef'
  surface-container-high: '#e8e8e9'
  surface-container-highest: '#e2e2e3'
  on-surface: '#1a1c1d'
  on-surface-variant: '#3e4850'
  inverse-surface: '#2f3132'
  inverse-on-surface: '#f0f1f2'
  outline: '#6f7881'
  outline-variant: '#bec8d1'
  surface-tint: '#00658f'
  primary: '#00658f'
  on-primary: '#ffffff'
  primary-container: '#24a1de'
  on-primary-container: '#00344c'
  inverse-primary: '#86cfff'
  secondary: '#0060a9'
  on-secondary: '#ffffff'
  secondary-container: '#4aa1fe'
  on-secondary-container: '#003663'
  tertiary: '#006397'
  on-tertiary: '#ffffff'
  tertiary-container: '#4e9eda'
  on-tertiary-container: '#003350'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#c8e6ff'
  primary-fixed-dim: '#86cfff'
  on-primary-fixed: '#001e2e'
  on-primary-fixed-variant: '#004c6d'
  secondary-fixed: '#d3e4ff'
  secondary-fixed-dim: '#a2c9ff'
  on-secondary-fixed: '#001c38'
  on-secondary-fixed-variant: '#004881'
  tertiary-fixed: '#cce5ff'
  tertiary-fixed-dim: '#92ccff'
  on-tertiary-fixed: '#001d31'
  on-tertiary-fixed-variant: '#004b73'
  background: '#f9f9fa'
  on-background: '#1a1c1d'
  surface-variant: '#e2e2e3'
typography:
  display-lg:
    fontFamily: Inter
    fontSize: 32px
    fontWeight: '700'
    lineHeight: 40px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Inter
    fontSize: 24px
    fontWeight: '600'
    lineHeight: 32px
    letterSpacing: -0.01em
  headline-sm:
    fontFamily: Inter
    fontSize: 20px
    fontWeight: '600'
    lineHeight: 28px
  body-lg:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: '400'
    lineHeight: 24px
  body-md:
    fontFamily: Inter
    fontSize: 14px
    fontWeight: '400'
    lineHeight: 20px
  label-md:
    fontFamily: Inter
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.01em
  headline-lg-mobile:
    fontFamily: Inter
    fontSize: 28px
    fontWeight: '700'
    lineHeight: 36px
rounded:
  sm: 0.25rem
  DEFAULT: 0.5rem
  md: 0.75rem
  lg: 1rem
  xl: 1.5rem
  full: 9999px
spacing:
  base: 4px
  xs: 8px
  sm: 12px
  md: 16px
  lg: 24px
  xl: 32px
  container-max: 1200px
  gutter: 16px
---

## Brand & Style
The design system is engineered for a fast-paced, high-engagement Telegram Quiz platform. The brand personality is **efficient, tech-forward, and accessible**, mirroring the immediacy of instant messaging. 

The design style is **Corporate Modern with a Native Twist**, drawing heavily from the Telegram desktop and mobile interfaces to ensure a zero-friction transition for users. It prioritizes functional clarity through high-quality typography and a disciplined use of whitespace. The emotional response should be one of reliability and speed, making the complex task of live quiz hosting feel lightweight and intuitive.

## Colors
The color palette is anchored by **Telegram Blue (#24A1DE)**, used for primary actions, progress indicators, and active states.

- **Primary:** Telegram Blue is used for the "Hero" actions and brand presence.
- **Secondary:** A slightly deeper blue (#3390EC) is utilized for hover states and links to provide subtle interactive feedback.
- **Neutral:** A range of soft grays (from #F4F4F5 for backgrounds to #707579 for secondary text) ensures the interface feels "airy" and reduces cognitive load during intense quiz sessions.
- **Surface:** Pure white (#FFFFFF) is reserved for cards and input fields to maintain high contrast against the neutral background.
- **Status:** Success (Green #31B131), Warning (Amber #F2994A), and Destructive (Red #E53935) follow standard Telegram protocol.

## Typography
This design system utilizes **Inter** for its exceptional legibility at small sizes and its neutral, systematic aesthetic. 

- **Hierarchy:** Use `display-lg` for quiz titles and `headline-md` for question headers. 
- **Body Text:** Use `body-lg` for quiz descriptions and `body-md` for general UI text and metadata.
- **Labels:** Use `label-md` in all-caps for small metadata like "QUIZ TYPE" or "TIMER" to distinguish it from interactive body text.
- **Scaling:** On mobile devices, large headlines should scale down to `headline-lg-mobile` to prevent awkward line breaks in quiz questions.

## Layout & Spacing
The layout follows a **Fluid Grid** model with a soft 4px baseline rhythm. 

- **Structure:** Content is housed in a central container with a max-width of 1200px for desktop. 
- **Margins:** A standard 16px gutter is used globally. Mobile views utilize 16px side margins, while tablet and desktop increase this to 24px or 32px to provide more breathing room.
- **Rhythm:** Use `lg` (24px) for vertical spacing between distinct cards and `sm` (12px) for internal padding within components like quiz options or input fields.

## Elevation & Depth
Depth is conveyed through **Tonal Layers** and **Ambient Shadows** to mimic the stackable nature of chat interfaces.

- **Level 0 (Background):** Neutral background color (#F4F4F5).
- **Level 1 (Cards/Surfaces):** Pure white with a very soft, highly diffused shadow (0px 2px 8px rgba(0, 0, 0, 0.05)). This is the primary surface for quiz content.
- **Level 2 (Modals/Overlays):** White with a more pronounced shadow (0px 10px 30px rgba(0, 0, 0, 0.12)) to lift the element above the main interface.
- **Interactions:** Hover states on cards should slightly deepen the shadow, rather than changing the background color, to maintain a clean aesthetic.

## Shapes
The shape language is consistently **Rounded**, reflecting the bubble-style UI of Telegram. 

- **Base Radius:** 0.5rem (8px) is the standard for most components including input fields and small buttons.
- **Large Radius:** 1rem (16px) is used for cards and main container sections to create a friendly, modern container for quiz content.
- **Pill Radius:** Used exclusively for tags, status indicators (e.g., "Live"), and primary "Start Quiz" buttons to give them a distinct, tactile feel.

## Components
- **Buttons:** Primary buttons use the pill shape with a solid #24A1DE fill and white text. Secondary buttons are transparent with a 1px #24A1DE border.
- **Quiz Preview Cards:** Large white cards with `rounded-lg` corners. They feature a prominent title, a small `label-md` for the number of questions, and a clear "Join" or "Play" button.
- **Input Fields:** Use a light gray background (#F4F4F5) with no border in their default state, switching to a 1.5px blue border when focused.
- **Status Indicators:** "Live" indicators should be a pill-shaped badge with a pulsing green dot, mimicking the Telegram "Online" status.
- **Lists:** Clean, borderless rows separated by subtle 1px dividers (#EEEEEE), featuring trailing icons (like a chevron) to indicate drill-down actions.
- **Checkboxes/Radios:** Circular for both to match Telegram’s polling style. Use the primary blue for the selected state with a crisp white checkmark or inner dot.