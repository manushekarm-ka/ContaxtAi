# Implementation Plan - Stage 3: Dynamic UI and Suggested Actions

This stage focuses on connecting the existing clipboard classification logic to the UI, enabling dynamic content type display and suggested actions based on the clipboard content.

## User Review Required

> [!IMPORTANT]
> The UI currently has unresolved references (e.g., `previewText` instead of `latestClipboard`). I will fix these as part of this stage to ensure the app compiles and runs correctly.

## Proposed Changes

### UI Layer

#### [MODIFY] [ContextAiScreen.kt](file:///C:/Users/manus/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/ui/ContextAiScreen.kt)
- Fix the `Unresolved reference 'previewText'` error by using `latestClipboard`.
- Update `ContentTypeCard` to accept and display the `DetectedContentType`.
- Remove the "Coming soon" placeholders for suggested actions.
- Implement dynamic rendering of `SuggestedActionCard` based on `uiState.suggestedActions`.
- Update the "Suggested actions" header text to reflect that they are now available.

### Data Layer (Optional/Cleanup)

#### [MODIFY] [ClipboardViewModel.kt](file:///C:/Users/manus/AndroidStudioProjects/MyApplication/app/src/main/java/com/example/myapplication/clipboard/ClipboardViewModel.kt)
- Verify if any additional logic is needed for handling action clicks (though the requirement is just to implement Stage 3, which is likely the display of these actions).

## Verification Plan

### Automated Tests
- I will check for any existing tests and run them to ensure no regressions.
- I'll look for `ClipboardViewModelTest` if it exists.

### Manual Verification
- Deploy the app to the emulator.
- Copy different types of content (URL, Email, Phone number, Math) and verify that:
    - The "Detected content type" updates correctly with the appropriate emoji.
    - The "Suggested actions" list updates to match the detected content type.
    - The "Coming soon" placeholders are replaced with real actions.
