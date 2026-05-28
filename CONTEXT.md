# Viewers Module Context

## What this module is

`Viewers` is the visualization component module. It contains JavaFX-based stages/components for rendering MIDI/music data and interactive views.

Source visibility note:

- This module is open source and published on GitHub.
- It is the exception within the otherwise closed-source MelodyMatrix project.

## Core responsibilities

- Reusable visual stage abstractions
- Event-driven visual updates (MIDI/chord/playback events)
- Charting/graphs and interactive display controls
- UI helpers for zoom, toggles, sliders, and visualization settings
- Demo/test launcher flows for viewer validation

## Where to look first

- `Viewers/src/main/` for production viewer code
- `Viewers/src/test/` for tests
- `Viewers/README.md` for module-specific usage notes
- `Viewers/pom.xml` for dependencies/plugins

Likely package hotspots:

- `be.codewriter.melodymatrix.view`
- `be.codewriter.melodymatrix.view.event`
- `be.codewriter.melodymatrix.view.component`
- `be.codewriter.melodymatrix.view.helper`
- `be.codewriter.melodymatrix.view.test`

## Typical dependency profile

- JavaFX
- UI/theme component libraries
- Charting libraries
- Kotlin Coroutines / Serialization
- Log4j2

## Build and run

From repo root:

```bash
mvn -pl Viewers clean test
mvn -pl Viewers javafx:run
```

From `Viewers/`:

```bash
mvn clean test
mvn javafx:run
```

## Change guidance for AI/code contributors

- Keep rendering logic decoupled from domain/business rules.
- Use existing event models before introducing new event channels.
- Prioritize smooth UI updates and avoid blocking the JavaFX thread.
- Place generic viewer components here so `MainApplication` can reuse them.

