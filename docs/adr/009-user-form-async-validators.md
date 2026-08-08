# ADR 009: User Form Async Uniqueness Validators

## Status
Accepted

## Context
User create/edit form validates account/card number uniqueness via async GET `/users/check` (debounced 400ms).

## Decision
- On `patchValue` (edit mode), async validators fire even for unchanged values → two GET `/users/check` per open.
- Harmless (return "available" for own numbers), but wasteful.
- **Upgrade**: set values with `emitEvent: false` to skip validator trigger on initial load.

## References
- `UserFormComponent.fillForm()` (ponytail on line 182)
- `UserFormComponent.checkUnique()`