# AGENTS.md — LLM Meta-Agent

## Theme / Project
A Spring Boot (Java 21) multi-agent AI app that queries two specialized LLMs (RecursiveFlint / NexAGI and NorthCode / cohere) in parallel, then synthesizes a final answer through a third LLM (Nvidia Nemotron). Uses Spring AI + OpenRouter-compatible API, JWT stateless auth via HttpOnly cookies, and Thymeleaf-free static HTML frontend.

## File-operation constraint (read first)
Do not modify, create, delete, or operate on any file unless the user explicitly names that specific file. If you do not know which file the user means, ask the user before touching anything. This applies to code, config, docs, and build files alike.

## Build & run
- Build tool: Maven wrapper (`mvnw` on Linux/macOS, `mvnw.cmd` on Windows)
- Run: `./mvnw spring-boot:run` (Linux/macOS) / `mvnw.cmd spring-boot:run` (Windows)
- Port: `8080`
- Docker build: `mvn clean package -DskipTests`, then `java -jar target/LLM-Meta-Agent-0.0.1-SNAPSHOT.jar` (Dockerfile copies the jar as `app.jar`)
- Tests: only `LlmMetaAgentApplicationTests.contextLoads()` exists — no real test suite. Do not invent or run additional tests unless asked.

## Required environment variables
Set before running (app will fail without them):
- `SPRING_OPENAI_API_KEY` — OpenRouter API key (starts with `sk-or-...`)
- `SPRING_OPENAI_BASE_URL` — `https://openrouter.ai/api/v1`

Mapped in `src/main/resources/application.properties` as `spring.ai.openai.api-key` / `spring.ai.openai.base-url`.

## Package & module boundaries
- Single module, single package tree: `com.llm.LLM.Meta_Agent` (note: not `com.example`)
- Entry point: `LlmMetaAgentApplication.java`
- Controller: `Controller/LLMMetaAgentController.java` (`@RequestMapping("/api")`)
- Service: `Service/LLMMetaAgentService.java`
- Config: `Configuration/LLMMetaAgentConfiguration.java`, `LLMMetaAgentSecurityConfig.java`, `JwtAuthenticationFilter.java`
- JWT service: `Service/LLMMetaAgentJwtService.java`

## Model wiring (non-obvious)
- Three `OpenAiChatModel` beans in `LLMMetaAgentConfiguration.java` with qualifiers:
  - `@Qualifier("recursiveflint")` → model `recursive/flint-v1.0:free`
  - `@Qualifier("northcode")` → model `cohere/north-mini-code:free`
  - `@Qualifier("nvidia")` → model `nvidia/nemotron-3.5-super-vl:free`
- OpenAI autoconfig is excluded: `spring.autoconfigure.exclude=org.springframework.ai.autoconfigure.openai.OpenAiautoConfiguration`
- `MessageWindowChatMemory` (max 10 messages) scoped per authenticated user for NorthCode only.
- Nvidia endpoint (`/api/nvidia/{message}`) runs RecursiveFlint and NorthCode concurrently via `CompletableFuture`, then sends both + user prompt to Nvidia for synthesis.
- System preprompt loaded from `src/main/resources/Preprompt` (classpath resource).

## API endpoints (authenticated unless noted)
- `POST /api/login` — public, sets `JWT_TOKEN` HttpOnly cookie
- `GET /api/logout` — clears cookie, forwards to `/login.html`
- `GET /api/recursiveflint/{message}` — RecursiveFlint/NexAGI directly
- `GET /api/northcode/{message}` — NorthCode directly (with chat memory)
- `GET /api/nvidia/{message}` — meta-agent: parallel sub-model fetch + Nvidia synthesis
- `GET /api/nvidia/firstmodelresponse` — last RecursiveFlint response
- `GET /api/nvidia/secondmodelresponse` — last NorthCode response
- `GET /` / `/api/` — forwards to `/index.html`

## Static assets
HTML pages live in `src/main/resources/static/` (`index.html`, `login.html`, `logintest.html`, `test.html`). No template engine; static resources served directly. All styles are inline `<style>` blocks inside each HTML file — there are **no separate `.css` files**.

## UI & Theme (detailed)

### Theme name & files
- Theme: **Terminal** (near-black green-cast ground, pale phosphor ink, one bright phosphor signal, mono UI, square surfaces, hairline rules, no glass/glow).
- Main console: `src/main/resources/static/index.html` — all CSS + JS inline in one file.
- Sign-in page: `src/main/resources/static/login.html` — separate inline CSS + JS, two-column layout.
- Logo/badge asset used on both: an inline SVG (16×16 path) — no external image files.

### Color system (CSS custom properties in `:root`)
All colors are declared once as `oklch()` tokens (and a few `rgba()` fallbacks). Key groups:
- Grounds: `--bg` (12.5% luminance), `--bg-1`, `--panel`, `--panel-2`, `--panel-solid`
- Rules/borders: `--line`, `--line-strong`
- Ink: `--ink` (94%), `--ink-dim` (82%), `--ink-faint` (64%)
- Accent (phosphor signal): `--acc` (oklch 87% 0.2 142, lime-green), `--acc-2` (`#4fd1c5`, teal), `--acc-grad` (135° gradient `#c8f34a` → `#3fd6c2`)
- Utility whites: `--white-03` … `--white-65`
- Glows: `--glow-1` … `--glow-9`, `--acc-glow`
- Warn: `--warn` (alias of `--ink`), `--warn-soft`, `--warn-line`
- Buttons: `--btn-ink` (`#071007`, near-black text on accent)

There is **one accent hue only** (green/phosphor). No second brand hue. If changing the theme, edit the `:root` block in `index.html` (and mirror in `login.html`).

### Typography
- Single font: **Martian Mono** (Google Fonts, variable axis `wdth`/`wght`), loaded via `<link href="https://fonts.googleapis.com/css2?family=Martian+Mono:wdth,wght@75..112.5,400..700&display=swap">`
- Fallbacks: `ui-monospace, SFMono-Regular, Cascadia Mono, Menlo, Consolas, monospace`
- CSS vars: `--font-ui`, `--font-display`, `--font-mono` all point to Martian Mono
- `--weight-display: 500`, `--stretch-display: 112.5%`, `--stretch-body: 87.5%`
- Everything is monospace. No serif/sans mixing.

### Layout architecture (index.html console)
Single-page app shell, all in one HTML file (no routing framework):

| Area | Class / ID | Purpose |
|------|-----------|---------|
| Shell | `.app` | CSS grid, `272px minmax(0, 1fr)` sidebar + canvas |
| Sidebar | `.sidebar` (`#sidebar`) | Brand, New chat button, Recent history list, Engine picker, About/Sign out |
| Canvas | `.canvas` | Main chat area: chatbar + log + composer |
| Chatbar | `.chatbar` | Model title, live status dot, session timer, Synthesis toggle, Clear button |
| Conversation log | `.log` (`#chat-log`) | Scrollable message list, `#chat-log-inner` |
| Empty state | `.empty` | "One question, several minds." hero + suggested prompt chips |
| Composer | `.composer` | Textarea input (`#user-input`), Send button (`#send-btn`), typing placeholder |
| Synthesis drawer | `.synth-drawer` (`#synth-drawer`) | Right-side panel showing NorthCode → Meta-Agent ← RecursiveFlint flow + source cards |
| About modal | `.modal` (`#about-modal`) | Lazy Three.js 3D "synthesis cluster" scene + model roster |
| Backdrop | `.backdrop` (`#backdrop`) | Dims behind open drawer/sidebar on mobile |

### Key UI components & behavior
- **Model picker** (`#picker`): Custom dropdown over a hidden `<select>` (`#model-select`). Options: `nvidia` (Meta-Agent / meta), `northcode` (single), `recursiveflint` (single). Switching updates the chatbar label, model chip, live dot (pulse = meta mode), and shows/hides the synthesis drawer path.
- **Synthesis drawer**: Fixed right panel (`340px`). Shows a flow path (NorthCode → Meta-Agent → RecursiveFlint), two `source-card` panels (`insight-northcode`, `insight-recursiveflint`) that flip from "Waiting for a query…" to "Synthesized by Meta-Agent" during a meta query. Opened via the Synthesis chip (`#synth-toggle`).
- **Messages**: `.msg--user` (right-aligned, gradient bubble), `.msg--ai` (left-aligned), `.msg--err` (warn-colored). AI responses stream word-by-word via `revealAi()` (38ms/step) with a blinking caret (`<span class="caret">`). HTML is sanitized with DOMPurify (allowed tags: b, strong, i, em, u, br, p, ul, ol, li, h1–h6, code, pre, blockquote, a, table, hr, span).
- **Thinking indicator**: `.think` with two spinning rings and three blipping dots ("Consulting models").
- **Send button**: `.sendbtn` — circular, accent gradient, ripple micro-interaction on pointerdown, disabled during loading.
- **Typewriter placeholder**: `.ph-type` types "Ask anything…" then clears, loops when input is empty and unfocused.
- **Conversations**: Frontend-only, persisted to `localStorage` under key `recursiveflint_conv_v1`. Each conversation stores id, title, engine, createdAt, messages. Sidebar renders history; clicking restores messages with GSAP staggered entrance + scramble-reveal text effect.
- **Session timer**: `T+HH:MM:SS` in chatbar, counts from page load.
- **Boot sequence**: Sidebar `.boot` types "initializing engines … ok", "loading session … ok", then fades out.

### External libraries (loaded via CDN, no bundler)
| Library | Version | Purpose |
|---------|---------|---------|
| Google Fonts (Martian Mono) | — | Typography |
| DOMPurify | 3.0.9 | HTML sanitization of AI responses |
| GSAP | 3.12.5 | Entrance animations, intro timeline, message stagger |
| Three.js | r128 | Lazy 3D "synthesis cluster" in About modal (canvas, points + line segments + core sphere) |

No Node, no npm, no build step for frontend. Edit HTML inline styles directly.

### Responsive breakpoints
- `<=1180px`: sidebar narrows to 248px, tighter padding
- `<=980px`: sidebar becomes off-canvas drawer (fixed, `translateX(-100%-24px)`), single-column grid, hamburger menu button appears
- `<=760px`: composer/log padding tightens, model row stacks to 1 column
- `<=480px`: further compacting (chip padding, modal padding, etc.)
- `prefers-reduced-motion: reduce`: all animations/transitions set to `0.01ms`, orbs paused, drawer/sidebar transitions disabled

### Theme variants
Inside `index.html` there is a second comment block `/* Hallmark · Terminal (manual deal) */` (around line 819) that overrides the default theme: square corners (`border-radius: 0`), no backdrop-filter, no box-shadow, accent buttons solid with `--bg` text, dotted link underlines, etc. This appears to be an alternate "console" skin applied via CSS overrides rather than a class toggle — check which rules are actually active before assuming both variants coexist.

### Login page (`login.html`)
- Two-column shell: left brand/status pane (`--color-paper-2`), right form pane
- Same terminal color tokens (prefixed `--color-*`), same Martian Mono font
- Faint CSS-grid background texture (`.tex-grid`)
- Form posts natively to `/api/login`; JS progressively enhances with `fetch()` + inline error display (`#errline`)
- Decorative pixel cat rendered on a `<canvas>` (`#cat`, 20×16 sprite)
- Typewriter heading ("LLM META AGENT"), scramble-reveal text, boot log, UTC clock
- Default dev creds: `user`/`user`, `admin`/`admin`

### Files to touch for UI changes
- Console UI/theme: `src/main/resources/static/index.html` (CSS in `<style>`, JS in `<script>`)
- Login UI/theme: `src/main/resources/static/login.html`
- No separate CSS/JS/build config to update.

## Default credentials (dev only)
- `user` / `user`
- `admin` / `admin`

## CI / lint / format
None configured. No GitHub Actions, no pre-commit, no formatter plugin, no typecheck beyond Maven compile.
