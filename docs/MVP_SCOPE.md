# MVP Scope

## MVP goal

The goal of the MVP is to validate whether users find value in a travel planning experience centered on **trip fit**, not just itinerary generation.

More specifically, the MVP aims to test whether users:
- value lightweight onboarding before planning
- understand the difference between their baseline travel profile and a generated trip profile
- find fit visualization and explanation helpful
- want to refine a trip multiple times until it feels more suitable

## What the MVP must prove

The MVP should prove that Taily can support the following core loop:

**welcome / entry → onboarding → trip input → itinerary generation → fit analysis → multi-round refinement**

If this loop feels useful and intuitive, the core product concept is validated.

## P0 scope

### 1. Welcome and entry
Taily provides a landing experience with:
- product introduction
- tagline
- entry into the planning flow

The MVP should distinguish between:
- first-time users, who go through onboarding
- returning users, who go directly to trip input

For the MVP, this behavior is simulated through local session state instead of full authentication.

### 2. Lightweight onboarding
First-time users complete a short onboarding flow to establish a baseline travel profile across six dimensions:
- Budget
- Pace
- Food Focus
- Sightseeing
- Comfort
- Exploration

### 3. Baseline profile creation
After onboarding, Taily creates a basic user profile that will inform trip generation and later fit analysis.

For the MVP, this profile may also be shown in a lightweight visual form, such as:
- a radar chart
- a short written interpretation

### 4. Trip input
Users provide a single natural-language trip request through the main input box.

The input may include:
- destination
- duration
- budget
- must-pass locations
- must-eat preferences
- must-see or must-do items
- lodging preferences
- pace preferences
- any other travel goals

The page should also include lightweight inspirational suggestion chips.

### 5. Itinerary generation
Taily generates a structured first-pass itinerary based on:
- the current trip request
- the user’s baseline travel profile

### 6. Loading state
The MVP includes a lightweight loading state that communicates the system is:
- understanding the user
- planning the trip
- evaluating fit

### 7. Trip result page
The result page includes:
- trip summary
- extracted preferences / key requirements
- itinerary by day
- fit analysis
- lightweight identity layer
- refinement actions

### 8. Fit analysis
Taily compares the user profile with the generated trip profile using the same six dimensions.

The result page should show:
- user profile radar chart
- trip profile radar chart
- overall fit score and label
- top matches
- top mismatches
- short fit explanation

### 9. Lightweight identity layer
The MVP may include:
- a traveler archetype
- a trip companion-pet archetype

Each should include:
- a name
- a short description

This layer should remain lightweight and supportive, not central to the MVP validation.

### 10. Multi-round refinement
Users can iteratively refine the generated trip.

The MVP refinement model includes:
- structured adjustments to selected trip dimensions
- free-text refinement input

Each refinement should:
- regenerate the trip
- update the fit analysis
- remain inside the same result experience
- return a full updated result

### 11. Recommended expansion points
The result page should include:
- day-level optional highlights
- trip-level worth-considering suggestions

This supports both local and overall trip expansion.

## P1 scope

These are valuable but not required for the MVP:
- real authentication instead of lightweight local-state simulation
- editable user profile after onboarding
- richer onboarding interpretation
- more polished loading experience
- stronger visual treatment for traveler and pet identity
- saving user trips to a database
- session persistence across devices
- map integration based on named stops
- richer recommendation explanations

## Out of scope for the first version

The first version is intentionally narrow.  
The following areas are excluded from the MVP in order to keep the build focused on validating the core trip-fit workflow:

- social features
- external review or social media integration
- full long-term memory
- advanced implicit profile learning
- collaborative planning
- booking flows
- imported screenshots, PDFs, or map pins
- real-time price optimization
- rich generated avatars or pet visuals
- version history and side-by-side trip comparison
- profile evolution notifications
- automatic user profile updates based on refinement behavior

## MVP success criteria

The MVP can be considered successful if users can:
1. complete onboarding with low friction
2. generate a trip without confusion
3. understand the fit analysis without extra explanation
4. identify at least one mismatch they care about
5. refine the trip multiple times in a natural way
6. feel that the updated trip is moving closer to what they want

## MVP design principle

The MVP should prioritize:
- clarity over completeness
- usability over feature count
- differentiation over breadth
- iteration over polish

The first version is not meant to be a full travel platform.  
It is meant to validate Taily’s core promise:

**plan a trip, understand how well it fits you, and keep refining until it feels right.**