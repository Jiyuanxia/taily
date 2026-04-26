# Design Reference

This document collects the current visual and structural references for Taily’s MVP.

Its purpose is to help implementation stay aligned with the product flow, layout priorities, and overall tone, without treating the current designs as pixel-perfect final UI.

---

## Overall design direction

Taily should feel:
- calm
- lightweight
- personal
- modern
- soft
- travel-oriented, but not like a booking platform

The UI should feel more like an intelligent travel companion than a traditional form-heavy planner.

The product should not feel like:
- a booking engine
- a dense dashboard
- a generic AI chat wrapper
- a travel comparison table

Instead, it should feel like:
- a guided planning companion
- a personalized travel product
- an experience centered on fit and refinement

---

## Navigation (MVP)

Navigation should support orientation and “Home” access without making the product feel like a dashboard.

Implementation guidance:
- Keep a minimal top header with a clear **Home** affordance (e.g., the Taily mark/title as a real button).
- If adding page links, keep them few, calm, and product-like (e.g., Trip planning and Profile Reveal).
- Avoid dashboard patterns like sidebars, heavy tables, or many simultaneous actions.

---

## Available design references

Current reference assets include:
- Welcome page
- Onboarding flow
- Trip input page
- Result page
- Page map
- User flow diagram
- High-level system diagram
- Request flow diagram
- Logic diagram

These assets should be treated as directional references for implementation.

---

## Page references

### Welcome page
The welcome page establishes the first impression of Taily.

It should:
- feel warm and clear
- communicate the value proposition quickly
- make the product feel personal rather than technical
- invite the user into the planning flow with low friction

Implementation should preserve:
- product name visibility
- tagline clarity
- lightweight entry feeling

---

### Onboarding flow
The onboarding flow is designed to feel like quick travel-style calibration, not a long setup form.

It should:
- ask one lightweight question at a time
- use button-based answers
- keep a clear sense of progress
- help the user feel understood early

Implementation should preserve:
- six travel dimensions
- low-friction interaction
- simple answer choices
- fast progression through the flow

The onboarding should not feel like a survey or a registration form.

---

### Trip input page
The trip input page should center around one main natural-language input box.

This is one of the most important product decisions in Taily.

Implementation should preserve:
- one large central prompt box
- no separate visible destination, budget, or duration fields in the main UI
- suggestion chips for brainstorming and inspiration
- a clean, spacious layout

The trip input experience should feel:
- conversational
- inspiring
- simple
- AI-first

It should not feel like a traditional travel booking form.

---

### Result page
The result page is the core product screen and the most important page in the MVP.

Implementation should preserve these main sections:
- trip summary
- extracted preferences / key requirements
- itinerary
- fit analysis
- identity layer
- trip-level suggestions
- refinement actions

The result page should make Taily’s differentiation obvious:
- not just a trip plan
- but a trip plan evaluated against the user’s travel style

Visually prioritize:
- fit analysis
- trip summary
- itinerary
- refinement actions

The page should be easy to scan, not dense or overly dashboard-like.

---

## Diagram references

### Page map
The page map shows the MVP page and state structure:
- welcome
- onboarding
- trip input
- loading
- result
- refined result state

This should guide implementation of page-level flow and routing or state transitions.

---

### User flow diagram
The user flow diagram shows:
- first-time user flow
- returning-user flow
- refinement loop

This should guide:
- entry logic
- onboarding logic
- result loop behavior

---

### High-level system diagram
The high-level system diagram shows the separation between:
- frontend
- backend
- AI layer

This should guide implementation boundaries and avoid putting AI logic directly in the frontend.

---

### Request flow diagram
The request flow diagram shows how:
- onboarding creates the baseline profile
- trip generation creates itinerary plus fit analysis
- refinement updates the trip and fit result

This should guide API behavior and backend orchestration.

---

### Logic diagram
The logic diagram shows the relationship between:
- user profile
- trip profile
- fit analysis

This should guide:
- data contracts
- frontend rendering
- backend output structure

---

## Implementation constraints

When implementing the MVP, preserve these design intentions:

- Keep one main natural-language input on the trip input page
- Do not introduce separate visible fields for destination, budget, or duration in the main input UI
- Keep onboarding lightweight and button-based
- Keep fit analysis highly visible on the result page
- Keep traveler identity and pet identity **supportive**: v1 uses structured identity cards (tags, six symbolic slots, prompt disclosure) plus an image placeholder—not a dense illustration system or avatar editor
- Keep refinement inside the same result experience
- Favor clarity and structure over visual complexity

The current designs are directional references, not strict final UI specifications.

---

## What can stay flexible

During implementation, these areas can remain flexible:
- exact spacing and typography
- exact color palette
- detailed component styling
- animation and motion polish
- degree of visual detail in traveler or pet expression

These can evolve later as long as the core product structure remains intact.

---

## What should not drift

These areas should not drift during implementation:
- the core trip-fit positioning
- the one-input travel request model
- the lightweight onboarding experience
- the visibility of fit analysis
- the refinement-centered interaction loop

These are central to Taily’s MVP identity and should remain stable across implementation.