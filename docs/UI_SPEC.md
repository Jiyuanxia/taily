# UI Spec

## UI goal

Taily’s UI should feel:
- calm
- modern
- lightweight
- personal
- clear
- slightly inspiring

It should not feel like a traditional booking form or travel search engine.
It should feel like the beginning of a conversation with an intelligent travel companion.

## Core page set

The MVP includes:
1. Welcome and entry
2. Onboarding
3. Trip input
4. Loading / generation state
5. Result page
6. Refined result state within the same result experience

---

## 1. Welcome and entry

### Purpose
Introduce Taily clearly and route users into the correct path.

### Main content
- Product name: Taily
- Tagline: Plan trips that fit you.
- Short product description
- Primary CTA button

### Behavior
- First-time users go to onboarding
- Returning users go directly to trip input
- In the MVP, this is simulated with local state rather than real authentication

### UI notes
- Keep layout simple and warm
- Do not overload with too much explanation
- Make the product positioning clear within a few seconds

---

## 2. Onboarding

### Purpose
Establish the user’s baseline travel profile through lightweight questions.

### Interaction model
- One question at a time
- Button-based answer choices only
- Minimal friction
- Clear progress indicator

### Six dimensions
1. Budget
2. Pace
3. Food Focus
4. Sightseeing
5. Comfort
6. Exploration

### Output
After onboarding, the frontend should be able to render:
- baseline profile state
- optional radar chart
- optional short profile interpretation
- lightweight traveler identity

### UI notes
- The flow should feel closer to calibration than form filling
- Use clean spacing and simple choices
- The user should always know how many steps are left

---

## 3. Trip input

### Purpose
Collect the current trip request in a natural, AI-first way.

### Main interaction
The trip input page uses one main natural-language input area only.

### Required elements
- Page title
- Short supportive subtitle
- One large central prompt box
- Inspirational suggestion chips
- Primary CTA button

### Important constraint
Do not use separate visible input fields for:
- destination
- duration
- budget
- preferences

These should all be entered naturally through the main input box.

### Suggestion chip examples
- I want a relaxed foodie trip
- Help me plan a city break
- I want more hidden gems
- I care about comfort

### Placeholder guidance
The prompt box should encourage users to describe:
- where they want to go
- how they like to travel
- budget preferences
- must-pass places
- must-eat or must-see items
- hotel preferences
- anything else that matters

### UI notes
- This page should feel inspiring, not intimidating
- Suggestion chips should support brainstorming
- The page should visually emphasize the single input box

---

## 4. Loading / generation state

### Purpose
Show that Taily is processing both planning and fit analysis.

### Suggested loading messages
- Understanding your travel style
- Planning your trip
- Matching this trip to you

### UI notes
- MVP loading can stay simple
- It should reinforce that Taily is doing more than generating an itinerary
- Do not over-invest in animation for the first version

---

## 5. Result page

### Purpose
Show the generated trip and make trip fit immediately understandable.

### Main sections

#### A. Trip summary
Must include:
- origin
- destination
- duration
- budget summary
- route summary
- one-line style summary

#### B. Extracted preferences / key requirements
Must show the most important preferences and constraints extracted from the user input.

These may include:
- must-pass stops
- must-eat items
- must-see or must-do items
- lodging preference
- budget preference
- pace preference

This section should help users feel understood.

#### C. Itinerary
The itinerary should be structured by day.

Each day may include:
- day number
- title
- area
- activities
- vibe note
- must-have coverage
- optional highlights

#### D. Fit analysis
Must include:
- user profile radar chart
- trip profile radar chart
- overall fit score
- overall fit label
- top matches
- top mismatches
- short explanation

This is the core differentiating section of the product.

#### E. Identity layer
Must include:
- traveler archetype
- trip companion-pet archetype

Each should include:
- name
- short description

**Identity v1 (structured cards):** surface **`travelerIdentitySpec`** / **`tripCompanionSpec`** (and/or the parallel archetype labels) with a portrait **placeholder** (no avatar builder), **mood** and **visual tags**, a compact **six-slot accessory legend**, and an optional disclosure for **`imagePrompt`** (demo / future image pipeline).

#### F. Trip-level suggestions
Must include:
- worthConsidering suggestions
- each suggestion should have a name and one-line reason

#### G. Refinement actions
Must allow:
- structured dimension adjustment
- natural-language refinement input
- full re-generation of the trip

### UI notes
- The page should be modular and easy to scan
- Fit analysis should be visually prominent
- Result clarity matters more than polish
- The identity layer should add personality without overwhelming the core utility

---

## 6. Refined result state

### Purpose
Let users iteratively improve the trip without restarting from scratch.

### Behavior
After refinement, the same result experience updates with:
- new trip summary
- updated itinerary
- updated fit analysis
- updated recommendations
- updated identity layer if needed

### UI notes
- Treat refinement as an update to the same experience
- Do not introduce complex version history in the MVP
- The user should clearly feel that the trip has changed in response to input

---

## Layout and style guidance

### General direction
- Soft, spacious layout
- Clean card-based information hierarchy
- Rounded shapes
- Friendly but not childish
- Consumer travel product feel

### Prioritize visually
- main prompt input
- fit analysis
- trip summary
- itinerary cards
- refinement actions

### Keep lightweight
- excessive animation
- heavy illustration systems
- over-detailed identity visuals
- complicated dashboard-like density

---

## Component hints

Likely reusable components:
- onboarding question card
- progress indicator
- suggestion chip
- large prompt box
- itinerary day card
- radar chart container
- fit summary card
- archetype card
- recommendation card
- refinement control block

---

## MVP UI principle

The UI should make one thing obvious:

**Taily does not just produce a trip plan.  
It helps the user understand and improve how well that trip fits them.**