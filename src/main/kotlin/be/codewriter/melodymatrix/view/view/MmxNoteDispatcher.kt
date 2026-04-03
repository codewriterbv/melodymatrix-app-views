package be.codewriter.melodymatrix.view.view

import be.codewriter.melodymatrix.view.event.NoteEventListener

interface MmxNoteDispatcher {
    var noteEventListener: NoteEventListener
    val midiChannel: Int get() = 0
}