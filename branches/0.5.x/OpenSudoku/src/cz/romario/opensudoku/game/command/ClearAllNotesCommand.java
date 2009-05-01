package cz.romario.opensudoku.game.command;

import java.util.ArrayList;
import java.util.List;

import cz.romario.opensudoku.game.SudokuCell;
import cz.romario.opensudoku.game.SudokuCellCollection;

public class ClearAllNotesCommand implements Command {

	private SudokuCellCollection mCells; 
	private List<NoteEntry> mOldNotes = new ArrayList<NoteEntry>();
	
	
	public ClearAllNotesCommand(SudokuCellCollection cells) {
		mCells = cells;
	}
	
	@Override
	public void execute() {
		mOldNotes.clear();
		for (int r = 0; r < SudokuCellCollection.SUDOKU_SIZE; r++) {
			for (int c = 0; c < SudokuCellCollection.SUDOKU_SIZE; c++) {
				SudokuCell cell = mCells.getCell(r, c);
				String note = cell.getNote();
				if (note != null && !note.equals("")) {
					mOldNotes.add(new NoteEntry(r, c, note));
					cell.setNote(null);
				}
			}
		}
	}

	@Override
	public void undo() {
		for (NoteEntry ne : mOldNotes) {
			mCells.getCell(ne.rowIndex, ne.colIndex).setNote(ne.note);
		}
		
	}
	
	private class NoteEntry {
		public int rowIndex;
		public int colIndex;
		public String note;
		
		public NoteEntry(int rowIndex, int colIndex, String note){
			this.rowIndex = rowIndex;
			this.colIndex = colIndex;
			this.note = note;
		}
		
	}
	

}