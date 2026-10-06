# Viewers Module Context

## What this module is

`Viewers` is the visualization component module. It contains JavaFX-based stages/components for rendering MIDI/music data and interactive views.

Source visibility note:

- This module is open source and published on GitHub.
- It is the exception within the otherwise closed-source MelodyMatrix project.

## Modules

- `model/` (`viewers-model`): domain and event model (`definition`, `event`, `data` packages). No JavaFX; the Maven enforcer fails the build if JavaFX reaches this module. The closed-source MelodyMatrix engine depends on it.
- `views/` (`viewers`): JavaFX views (`view.view.*`), shared components, helpers, i18n, `view.license.LicenseStatus`, and JavaFX colours for model enums (`view.color`). The LED strip talks to serial ports only through `view.view.ledstrip.serial.SerialPorts`; `JSerialCommPorts` is the only jSerialComm code.
- `demo/` (`viewers-demo`): standalone test application (`view.demo.TestLauncher`) with simulated MIDI and audio input.

This repository must build and run on its own (`mvn javafx:run` from the root starts the demo): never add a dependency on MelodyMatrix Engine or MainApplication, and do not link to their classes in KDoc.

## Build and run

From the MelodyMatrix repo root:

```bash
mvn -pl Viewers/views -am test
```

From `Viewers/`:

```bash
mvn clean test
mvn javafx:run      # starts the demo application
```

## Change guidance for AI/code contributors

- Keep rendering logic decoupled from domain/business rules.
- Use existing event models before introducing new event channels; new event types belong in `model/`.
- Prioritize smooth UI updates and avoid blocking the JavaFX thread.
- Place generic viewer components here so `MainApplication` can reuse them.

