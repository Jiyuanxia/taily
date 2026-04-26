# User Flow

## Flow overview

Taily’s MVP is built around a simple but differentiated loop:

**entry → baseline profile → trip request → itinerary generation → fit analysis → repeated refinement**

The product should feel:
- welcoming at first entry
- personalized from the beginning
- understandable in its reasoning
- easy to iterate on

---

## First-time user flow

### Step 1: User lands on Taily
The user arrives at the product and sees a clear value proposition:
Taily helps them plan trips that better match their travel style.

### Step 2: Entry routing
Taily determines whether the user is:
- a first-time user
- a returning user

For the MVP, this is implemented through lightweight local state rather than real authentication.

### Step 3: Onboarding for first-time users
If the user is new, they complete a short onboarding flow covering six travel dimensions:
- Budget
- Pace
- Food Focus
- Sightseeing
- Comfort
- Exploration

### Step 4: Baseline profile creation
After onboarding, Taily creates the user’s baseline travel profile.

This profile becomes the default reference point for both:
- itinerary generation
- fit analysis

The backend may also return:
- a profile summary
- a traveler archetype
- lightweight avatar configuration

### Step 5: Trip input
The user enters the current trip request.

The main interaction uses one natural-language input box.

The prompt may contain:
- destination
- duration
- budget
- must-pass places
- must-eat items
- must-see or must-do requests
- lodging preferences
- pace expectations
- any other travel goals

Suggestion chips may be shown to inspire or guide input.

### Step 6: Trip generation
Taily combines:
- the user’s baseline profile
- the current trip request

to generate an initial itinerary.

### Step 7: Fit analysis
The itinerary is evaluated using the same six dimensions as the user profile.

This produces:
- a trip profile
- an overall fit result
- a short explanation of what matches and what does not

### Step 8: Result page
The result page shows:
- trip summary
- extracted preferences / key requirements
- itinerary
- fit visualization
- fit explanation
- traveler archetype
- trip companion-pet archetype
- recommendations and optional highlights
- refinement actions

### Step 9: User decides whether to refine
Instead of starting over, the user can adjust the trip through:
- structured dimension changes
- additional natural-language refinement input

---

## Returning user flow

### Step 1: Returning user enters Taily
A returning user should skip onboarding and go directly to trip input.

### Step 2: Existing baseline profile is reused
Taily uses the previously established travel profile as the default personalization baseline.

### Step 3: User submits a new trip request
The same planning and fit flow then continues from trip input through result and refinement.

For the MVP, this flow is simulated with local state instead of full authentication and persistent account logic.

---

## Core planning loop

The core loop of the MVP is:

1. Understand who the traveler is  
2. Understand what trip they want now  
3. Generate a trip  
4. Compare the trip to the traveler’s style  
5. Help the traveler refine the fit  
6. Repeat until the trip feels right

This is the main behavioral loop Taily is designed to validate.

---

## Refinement flow

The refinement flow should feel lightweight, repeatable, and intuitive.

The user should not need to restart the planning flow after seeing the first result.

### Refinement inputs
The MVP refinement model combines:
- structured dimension adjustments
- natural-language refinement text

### Example refinement actions
- make it more relaxed
- lower the budget
- add more food focus

### Example refinement prompt
“Keep the Disney day, but make the overall trip feel less rushed and slightly more comfortable.”

### Refinement sequence
1. The user reviews the current trip
2. The user sees which dimensions match and which do not
3. The user adjusts selected trip dimensions and/or adds a refinement prompt
4. Taily regenerates the itinerary
5. Taily updates fit analysis and recommendations
6. The user reviews the new result
7. The user may refine again if needed

### Product principle
Refinement should happen within the same result experience, not through a full restart of the flow.

---

## Key interaction decisions

### 1. Welcome comes before personalization
The product first introduces itself clearly, then begins personalization.

### 2. Onboarding happens only for first-time users
This helps Taily build a useful baseline without slowing down returning users.

### 3. Trip input remains lightweight
The product should avoid heavy forms and preserve natural-language flexibility.

### 4. Fit is revealed after planning
Users first receive a generated trip, then receive help interpreting whether it suits them.

### 5. Refinement is central to the experience
The value of Taily is not just in the first generated result, but in helping users improve the fit over multiple rounds.

### 6. Identity is additive, not dominant
Traveler and pet expression should make the experience more memorable, but not distract from trip utility.

---

## MVP simplifications

To keep the first version focused, the MVP intentionally avoids:
- deep account management
- advanced profile editing
- multi-user collaboration
- rich visual avatar generation
- version history
- side-by-side trip comparison
- imported external planning materials
- external review aggregation
- automatic user profile evolution

The first goal is to validate the clarity and usefulness of the trip-fit flow.

---

## Primary user story

As an independent traveler, I want Taily to understand my travel style, generate a trip based on that style, and show me how well the trip fits me, so I can keep refining it until it feels right.

---

## Secondary user story

As a user who often feels uncertain about whether a travel plan is truly right for me, I want a visual and explainable way to compare my preferences with a proposed itinerary, so I can make travel decisions with more confidence.