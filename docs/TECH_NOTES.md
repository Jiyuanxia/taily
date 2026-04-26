# Tech Notes

## Technical goal

The technical goal of the MVP is to support Taily’s core trip-fit workflow through a frontend-backend separated architecture.

The first version should:
- validate the product concept on web
- keep AI logic centralized in the backend
- support future expansion to iOS and Android
- prioritize speed of MVP delivery over full production completeness

The MVP should use real AI-backed backend logic for the core product loop, while keeping non-essential platform features lightweight or simulated.

---

## Architecture principle

Taily should use a frontend-backend separated architecture so that the same product logic and AI orchestration can later support multiple client surfaces, including:
- web
- iOS
- Android

The backend should own:
- user profile logic
- itinerary generation logic
- fit analysis logic
- refinement logic
- AI orchestration
- future persistence and auth integration

The frontend should focus on:
- user input
- UI rendering
- state transitions
- user interactions
- lightweight local first-time vs returning-user behavior for the MVP

---

## MVP client strategy

### Current target
- Web first

### Future target
- iOS
- Android

### Reasoning
Web is the fastest way to validate the MVP and record demos, while a separated backend architecture makes it easier to support future multi-platform clients without rebuilding core planning logic.

---

## Suggested frontend stack

A practical MVP web stack could be:

- **Frontend framework:** React
- **Frontend tooling:** Vite
- **Styling:** Tailwind CSS or a simple CSS solution
- **Charting:** a radar chart library such as Recharts
- **State handling:** lightweight local state first
- **Routing:** simple client-side routing if needed

### Why this works
React is familiar enough to move quickly on the MVP, while Vite keeps the setup lightweight and fast. This is enough to support onboarding, trip input, fit visualization, and multi-round refinement on web.

---

## Suggested backend stack

A practical backend MVP stack could be:

- **Backend framework:** Spring Boot
- **Language:** Java
- **API style:** REST API
- **AI integration:** OpenAI API
- **Persistence:** optional at first, can be minimal or deferred
- **Auth:** skipped for MVP, simulated with local state on the frontend

### Why this works
Spring Boot is a stronger fit for the current skill set and makes it easier to build a clean service-based backend for profile creation, trip generation, fit analysis, and refinement.

---

## MVP session and identity logic

For the MVP, Taily does not need full authentication.

Instead, the product can simulate:
- first-time user flow
- returning-user flow

### Suggested MVP behavior
- Use local storage or simple local session state in the frontend
- Store whether onboarding has been completed
- Store a lightweight baseline profile locally for testing
- Optionally capture the user’s approximate current location on homepage visit (browser geolocation) and store it locally for future default-origin use
- Skip onboarding for returning users in the same browser environment

### Why this is acceptable
The purpose of the MVP is to validate product usefulness, not account management.

Future versions can replace this with real authentication and backend persistence.

---

## High-level system structure

### Frontend
Responsibilities:
- welcome page and onboarding UI
- trip input experience
- loading state
- result page rendering
- fit visualization rendering
- refinement interactions
- lightweight local first-time vs returning-user state

### Backend
Responsibilities:
- receive onboarding answers
- build baseline user profile
- receive trip requests
- generate itinerary
- analyze trip profile
- compare user profile and trip profile
- generate fit explanation
- process refinement requests
- return structured responses to the frontend

### AI layer
Responsibilities:
- preference interpretation
- itinerary generation
- itinerary profiling
- fit explanation
- refinement regeneration

The AI layer should be accessed only from the backend, not directly from the frontend.

---

## Core backend capabilities

The backend should support four main product capabilities.

### 1. Build baseline user profile
Input:
- onboarding answers across six dimensions

Output:
- structured user profile
- optional profile interpretation

### 2. Generate itinerary
Input:
- baseline user profile
- trip request details

Output:
- structured itinerary
- trip summary

### 3. Analyze trip fit
Input:
- baseline user profile
- generated itinerary

Output:
- trip profile
- fit score or fit summary
- fit explanation
- top matches
- top mismatches
- traveler archetype
- trip companion-pet archetype

### 4. Refine itinerary
Input:
- current user profile
- current itinerary
- refinement direction

Output:
- updated itinerary
- updated trip profile
- updated fit explanation
- updated archetype / pet labels if needed

---

## Suggested MVP API boundaries

The backend API should be organized around product capabilities, not screens.

### Recommended MVP endpoints
- `POST /profile/create`
- `POST /trip/generate-with-fit`
- `POST /trip/refine`
- `POST /identity/portrait` (optional OpenAI image generation from `travelerIdentitySpec` / `tripCompanionSpec` prompts; returns base64 PNG)

This keeps the frontend simpler and concentrates orchestration logic in the backend.

---

## Recommended API approach for MVP

### 1. `POST /profile/create`
Purpose:
- receive onboarding answers
- return baseline profile

Input:
- answers to six onboarding questions

Output:
- structured user profile
- optional radar-chart-ready data
- optional short interpretation

### 2. `POST /trip/generate-with-fit`
Purpose:
- generate trip and fit analysis in one request

Input:
- user profile
- trip request prompt
- optional origin context

Output:
- trip summary
- extracted preferences
- itinerary
- trip profile
- fit analysis
- traveler archetype
- trip companion-pet archetype
- global recommendations

### 3. `POST /trip/refine`
Purpose:
- update the current trip based on dimension adjustments and refinement text

Input:
- user profile
- current trip
- refinement object

Output:
- full updated trip result using the same structure as `generate-with-fit`

---

## End-to-end request flow

The full MVP request flow moves across the frontend, backend, profile service, trip service, and OpenAI integration.

It includes three main paths:
1. onboarding answers to baseline profile creation
2. trip request to itinerary generation and fit analysis
3. trip refinement to updated itinerary and updated fit analysis

This flow reflects the core architectural decision of the MVP:
AI orchestration stays in the backend, while the frontend focuses on input, rendering, and lightweight session behavior.

---

## AI orchestration flow

The backend should orchestrate AI use in a controlled sequence.

### Flow 1: Onboarding to profile
1. Receive onboarding answers
2. Translate answers into structured user-profile values
3. Return normalized profile object

### Flow 2: Trip generation
1. Receive current trip request
2. Combine trip request with baseline user profile
3. Extract meaningful preferences and constraints from the prompt
4. Call AI to generate structured trip fields (preferences + itinerary + summary copy + suggestions)
5. Validate and normalize the AI output to match the stable response contract
6. If AI output is invalid or incomplete, fall back to deterministic first-pass generation
7. Return structured trip output

### Flow 3: Trip fit analysis
1. Analyze the generated itinerary using the same six dimensions
2. Compare user profile and trip profile
3. Generate fit explanation text (AI-backed when available)
4. Generate traveler archetype and pet archetype labels
5. Generate trip-level worth-considering suggestions

### Flow 4: Refinement
1. Receive dimension adjustments and refinement prompt
2. Reinterpret the itinerary in the context of:
   - the baseline user profile
   - the current trip
   - the selected changes
3. Generate an updated itinerary (MVP may preserve structure and apply smaller adjustments)
4. Re-run fit analysis (keep scoring deterministic for stability)
5. Return a full updated result (same structure as generate-with-fit)

---

## Output structure principles

Taily should use structured outputs from the backend so the frontend can reliably render:
- radar chart values
- itinerary sections
- fit explanations
- archetype labels
- pet labels
- extracted preferences
- day-level highlights
- trip-level recommendations

The frontend should not depend on raw freeform model text for key UI areas.

This makes the UI:
- more stable
- easier to test
- easier to adapt across future platforms

---

## Data decisions for MVP

### What can stay local for the MVP
- onboarding completion state
- baseline profile
- current itinerary session
- temporary refinement state

### What does not need backend persistence yet
- user accounts
- long-term preference history
- saved trips
- cross-device sync

### Future data model direction
In later versions, the backend can persist:
- user accounts
- user profiles
- saved itineraries
- refinement history
- implicit profile signals
- profile evolution events

---

## Visualization responsibilities

The frontend should render:
- user profile radar chart
- trip profile radar chart
- fit score and fit label
- itinerary cards or day blocks
- extracted preferences
- optional highlights
- worth-considering suggestions
- traveler and pet identity cards

The backend should return data in a frontend-friendly structure so chart rendering is simple and deterministic.

---

## MVP simplifications

To ship faster, the MVP should intentionally avoid:
- full authentication
- real cross-device persistence
- database-backed long-term user storage
- advanced profile learning
- automatic profile evolution updates
- external travel APIs
- external reviews integration
- heavy server-side portrait **storage** or CDN pipelines (MVP uses **base64 responses** + small **browser cache** by `specHash`; long-term asset hosting is out of scope)
- rich freeform companion-pet labeling from models (canonical pet names are enforced in API output)
- version history or side-by-side refinement comparison
- full map routing and coordinate handling

The first version should optimize for:
- speed of development
- clarity of core product behavior
- clean AI orchestration
- easy demoability

---

## Deployment direction

### MVP deployment
- React frontend deployed as a web app
- Spring Boot backend deployed separately as an API service

### Architecture note
Even though the MVP only targets web, the separated frontend-backend structure keeps the core product logic reusable for future iOS and Android clients.

---

## Technical tradeoff summary

Taily’s MVP should favor:
- familiar backend technology for faster execution
- a lightweight React frontend for web delivery
- centralized backend AI orchestration
- frontend-backend separation for future multi-platform support
- structured outputs for stable rendering

It should not over-invest yet in:
- new framework learning beyond what is necessary
- full authentication
- persistence-heavy account systems
- complex external integrations

The architecture should be future-aware, but chosen for practical execution speed.