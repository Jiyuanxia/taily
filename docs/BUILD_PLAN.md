# Build Plan

## Build strategy

Taily should be built by validating the core trip-fit loop first, not by trying to complete the whole product at once.

The implementation should prioritize:
- working end-to-end flow
- real AI-backed backend behavior
- stable structured responses
- basic but clear frontend rendering
- iterative refinement over visual polish

The first goal is to get one real flow working:

**onboarding → profile creation → trip input → generate-with-fit → result page → refinement**

---

## Phase 1: Project scaffolding

### Goal
Set up the frontend and backend foundations so development can proceed in parallel.

### Frontend tasks
- initialize React + Vite project
- create basic app structure
- create page / state structure for:
  - welcome
  - onboarding
  - trip input
  - loading
  - result
- add local storage helper for simulated first-time vs returning-user behavior

### Backend tasks
- initialize Spring Boot project
- create package structure for:
  - controller
  - service
  - dto
  - config
- create a simple health endpoint
- prepare OpenAI API integration skeleton

### Output of Phase 1
- frontend runs locally
- backend runs locally
- basic project structure is ready

---

## Phase 2: Onboarding and profile creation

### Goal
Build the first real backend-connected flow.

### Frontend tasks
- build onboarding UI
- implement six question flow
- store answers in local state
- send onboarding answers to backend
- receive and store profile response
- simulate returning-user behavior using local state

### Backend tasks
- implement `POST /profile/create`
- accept onboarding answers
- normalize answers into six-dimension score profile
- return:
  - user profile
  - profile summary
  - traveler archetype
  - avatar config

### Output of Phase 2
- onboarding works end-to-end
- user profile is generated from backend
- frontend can store and reuse profile

---

## Phase 3: Trip input and generate-with-fit

### Goal
Make the user able to submit a real trip request and receive a generated result.

### Frontend tasks
- build trip input page
- implement one main natural-language input box
- add suggestion chips
- submit trip request to backend
- show loading state while waiting for result

### Backend tasks
- implement `POST /trip/generate-with-fit`
- accept:
  - user profile
  - trip prompt
  - optional origin context
- call OpenAI
- extract:
  - preferences and constraints
  - itinerary
  - trip profile
  - fit analysis
  - traveler archetype
  - trip companion-pet archetype
  - worth-considering suggestions
- return structured response matching `DATA_CONTRACTS.md`

### Output of Phase 3
- user can go from trip input to a real generated result
- first full AI-backed end-to-end flow works

---

## Phase 4: Result page rendering

### Goal
Render the full result page in a stable and understandable way.

### Frontend tasks
- render trip summary
- render extracted preferences
- render itinerary by day
- render user radar chart
- render trip radar chart
- render fit summary
- render traveler archetype
- render pet archetype
- render worth-considering suggestions
- render day-level optional highlights

### Backend tasks
- stabilize response structure
- improve consistency of fit analysis
- make sure all required fields are returned in a frontend-friendly format

### Output of Phase 4
- result page is functionally complete
- the core differentiator of Taily is visible

---

## Phase 5: Refinement flow

### Goal
Make Taily iterative, not just generative.

### Frontend tasks
- add refinement controls
- allow structured dimension adjustments
- allow optional refinement text
- submit refinement request to backend
- replace current result with refined result

### Backend tasks
- implement `POST /trip/refine`
- accept:
  - user profile
  - current trip
  - refinement object
- regenerate itinerary
- rerun fit analysis
- return full updated result

### Output of Phase 5
- user can refine the trip in multiple rounds
- Taily’s core value becomes clearer than a one-shot generator

---

## Phase 6: Demo stabilization and polish

### Goal
Make the MVP stable enough to record and present.

### Frontend tasks
- improve spacing and readability
- clean up loading state
- handle basic empty and error states
- ensure refinement updates feel understandable
- keep UI clutter low

### Backend tasks
- improve output consistency
- handle malformed or vague prompts more gracefully
- verify schema stability across multiple test cases

### Output of Phase 6
- local demo is stable
- flow is understandable
- result page is clear enough for recording

---

## Phase 7: Deployment and presentation

### Goal
Turn the MVP into a shareable and presentable artifact.

### Tasks
- deploy frontend
- deploy backend
- verify end-to-end behavior in deployed environment
- record demo video
- update Notion with screenshots and final demo link
- prepare final README if needed

### Output of Phase 7
- shareable MVP
- demo-ready project
- strong PM + product case study asset

---

## Suggested implementation order

If development time is tight, use this exact order:

1. scaffold frontend and backend  
2. implement onboarding UI  
3. implement `/profile/create`  
4. store profile locally  
5. build trip input page  
6. implement `/trip/generate-with-fit`  
7. render minimal result page  
8. add radar chart rendering  
9. add extracted preferences and suggestions  
10. implement `/trip/refine`  
11. add refinement UI  
12. polish for demo

---

## MVP stop line

The MVP is ready for demo once the following works:

- onboarding creates a usable baseline profile
- trip input accepts a real natural-language request
- backend returns a structured generated trip
- result page shows trip fit clearly
- user can refine and receive a full updated result

Anything beyond that is improvement, not core validation.

---

## Build principle

Do not optimize for completeness first.  
Optimize for getting the core loop real, visible, and repeatable.

Taily becomes valuable once users can:
- feel understood
- see how well the trip fits them
- change the trip until it feels right