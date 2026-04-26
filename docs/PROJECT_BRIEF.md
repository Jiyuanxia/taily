# Taily Project Brief

## Product
Taily is an AI travel companion that helps users plan trips based on their travel style, understand how well a proposed itinerary fits them, and refine the trip accordingly.

## Core differentiation
Taily does not just generate itineraries.  
It helps users answer:

**Does this trip actually fit me?**

## Core product idea
The product combines:
- lightweight onboarding to establish a baseline travel profile
- AI-generated trip planning
- fit analysis based on six dimensions
- multi-round refinement until the trip feels more right

## MVP core loop
Welcome and entry  
-> first-time user onboarding or returning-user skip  
-> baseline user profile creation  
-> trip input  
-> itinerary generation  
-> fit analysis  
-> result page  
-> refinement  
-> updated result  
-> repeat until satisfied

## Six dimensions
Taily models both users and trips across these six dimensions:
- Budget
- Pace
- Food Focus
- Sightseeing
- Comfort
- Exploration

## Core user experience
1. The user arrives at Taily
2. First-time users complete a short onboarding flow
3. Taily builds a baseline profile
4. The user enters a trip request through one main natural-language input
5. Taily generates a trip and analyzes how well it fits the user
6. The result page shows the itinerary, fit analysis, and lightweight identity layer
7. The user can refine the trip through structured adjustments and text input
8. Taily returns a fully updated result

## MVP must include
- Welcome page
- Lightweight onboarding
- Baseline user profile creation
- Trip input page with one main prompt box
- Generated itinerary
- Fit analysis using six dimensions
- User profile and trip profile visualization
- Traveler archetype
- Trip companion-pet archetype
- Multi-round refinement
- Fake login / local-state first-time vs returning-user logic

## MVP constraints
The MVP should stay intentionally narrow.

It should not include:
- full authentication
- persistent account storage
- external review integration
- social features
- booking flows
- full long-term memory
- rich generated avatars
- advanced map routing
- side-by-side version comparison

## Technical direction
- Frontend: React + Vite
- Backend: Spring Boot
- Architecture: frontend-backend separated
- AI: real OpenAI API calls for core flows
- Session handling: local-state simulation for first-time vs returning-user behavior

## Design direction
Taily should feel:
- calm
- personal
- lightweight
- thoughtful
- modern
- travel-oriented, but not like a booking form

## Important product principle
User input can remain flexible and unstructured, but backend outputs should be normalized into a stable structure that reflects Taily’s six-dimension model.

## Goal of this project
Build the first working MVP of Taily as a real, extensible starting point for the product — not a fake demo-only prototype.