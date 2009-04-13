package cz.romario.opensudoku.gui;

import cz.romario.opensudoku.game.SudokuCell;
import cz.romario.opensudoku.game.SudokuCellCollection;
import cz.romario.opensudoku.game.SudokuGame;
import cz.romario.opensudoku.gui.EditCellDialog.OnNoteEditListener;
import cz.romario.opensudoku.gui.EditCellDialog.OnNumberEditListener;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

/**
 *  Sudoku board widget.
 *  
 * @author romario
 *
 */
public class SudokuBoardView extends View {

	private int cellWidth;
	private int cellHeight;
	
	private Paint linePaint;
	private Paint numberPaint;
	private Paint notePaint;
	private int numberLeft;
	private int numberTop;
	private Paint readonlyPaint;
	private Paint touchedPaint;
	private Paint selectedPaint;
	
	private SudokuCell touchedCell;
	private SudokuCell selectedCell;
	public boolean readonly = false;
	
	private EditCellDialog editCellDialog;
	
	private SudokuGame game;
	private SudokuCellCollection cells;
	
	private OnCellTapListener onCellTapListener;
	
	public SudokuBoardView(Context context) {
		super(context);
		initWidget();
	}
	
	public SudokuBoardView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		initWidget();
	}
	
	public void setGame(SudokuGame game) {
		this.game = game;
		setCells(game.getCells());
	}

	public void setCells(SudokuCellCollection cells) {
		this.cells = cells;
		if (!this.readonly) {
			this.selectedCell = this.cells.getCell(0, 0); // first cell will be selected by default
		}
		this.invalidate();
	}
	
	public SudokuCellCollection getCells() {
		return cells;
	}
	
	public SudokuCell getSelectedCell() {
		return selectedCell;
	}
	
	public void setReadOnly(boolean readonly) {
		this.readonly = readonly;
	}
	
	public boolean getReadOnly() {
		return readonly;
	}
	
	public void setOnCellTapListener(OnCellTapListener l) {
		onCellTapListener = l;
	}
	
	private void initWidget() {
		setFocusable(true);
		setFocusableInTouchMode(true);
		
		setBackgroundColor(Color.WHITE);
		
		linePaint = new Paint();
		linePaint.setColor(Color.BLACK);
		
		numberPaint = new Paint();
		numberPaint.setColor(Color.BLACK);
		numberPaint.setAntiAlias(true);

		notePaint = new Paint();
		notePaint.setColor(Color.BLACK);
		notePaint.setAntiAlias(true);
		
		readonlyPaint = new Paint();
		readonlyPaint.setColor(Color.LTGRAY);

		touchedPaint = new Paint();
		touchedPaint.setColor(Color.rgb(50, 50, 255));
		//touchedPaint.setColor(Color.rgb(100, 255, 100));
		touchedPaint.setAlpha(100);
		
		selectedPaint = new Paint();
		selectedPaint.setColor(Color.YELLOW);
		selectedPaint.setAlpha(100);
	}

	private int screenOrientation = -1;
	/**
	 * Ensures that editCellDialog exists and is properly initialized.
	 * 
	 * @return
	 */
	private void ensureEditCellDialog() {
		if (editCellDialog == null) {
			if (screenOrientation == -1) {
				//WindowManager wm = (WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
				//screenOrientation = wm.getDefaultDisplay().getOrientation();
				screenOrientation = getResources().getConfiguration().orientation;
			}
			
			// TODO: EditCellDialog is not ready for landscape
			if (screenOrientation != Configuration.ORIENTATION_LANDSCAPE) {
				editCellDialog = new EditCellDialog(getContext());
		        editCellDialog.setOnNumberEditListener(onNumberEditListener);
		        editCellDialog.setOnNoteEditListener(onNoteEditListener);
			}
		}
		
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        int width;
        int height;
        
        if (widthMode == MeasureSpec.EXACTLY) {
        	// Parent has told us how big to be.
        	width = widthSize;
        } else {
        	// TOOD: jaky dat default?
        	width = 100;
        }
        
        if (heightMode == MeasureSpec.EXACTLY) {
        	// Parent has told us how big to be.
        	height = heightSize;
        } else {
        	// TOOD: jaky dat default?
        	height = 100;
        }
        
        // sudoku will always be square
        // TODO: predpokladam ze vyska px je stejna jako sirka
        int size = Math.min(width, height);
        width = size;
        height = size;
        
        cellWidth = width / 9;
        cellHeight = height / 9;

        // TODO: zohlednovat padding
        setMeasuredDimension(cellWidth * 9 + 1, cellHeight * 9 + 1);
        
        numberPaint.setTextSize(cellHeight * 0.75f);
        notePaint.setTextSize(cellHeight / 3f);
        // compute offsets in each cell to center the rendered number
        numberLeft = (int) ((cellWidth - numberPaint.measureText("9")) / 2);
        numberTop = (int) ((cellHeight - numberPaint.getTextSize()) / 2);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		int width = getMeasuredWidth();
		int height = getMeasuredHeight();
		
		// draw cells
		int cellLeft, cellTop;
		if (cells != null) {
			
			// TODO: why?
			float numberAscent = numberPaint.ascent();
			float noteAscent = notePaint.ascent();
			float noteWidth = cellWidth / 3f;
			for (int row=0; row<9; row++) {
				for (int col=0; col<9; col++) {
					SudokuCell cell = cells.getCell(row, col);
					
					cellLeft = col * cellWidth;
					cellTop = row * cellHeight;

					// draw read-only field background
					if (!cell.getEditable()) {
						canvas.drawRect(
								cellLeft, cellTop, 
								cellLeft + cellWidth, cellTop + cellHeight,
								readonlyPaint);
					}
					
					// draw cell Text
					int value = cell.getValue();
					if (value != 0) {
						numberPaint.setColor(cell.getInvalid() ? Color.RED : Color.BLACK);
						canvas.drawText(new Integer(value).toString(),
								cellLeft + numberLeft, 
								cellTop + numberTop - numberAscent, 
								numberPaint);
					} else {
						
						// TODO: this is ugly temporary version
						if (cell.hasNote()) {
							Integer[] numbers = getNoteNumbers(cell.getNote());
							int r = 0, c = 0;
							if (numbers != null) {
								for (Integer number : numbers) {
									if (c == 3) {
										r++;
										c = 0;
									}
									
									canvas.drawText(number.toString(), cellLeft + c*noteWidth + 2, cellTop - noteAscent + r*noteWidth - 1, notePaint);
									
									c++;
								}
							}
						}
					}
					
					
						
				}
			}

			// highlight selected cell
			if (!readonly && selectedCell != null) {
				cellLeft = selectedCell.getColumnIndex() * cellWidth;
				cellTop = selectedCell.getRowIndex() * cellHeight;
				canvas.drawRect(
						cellLeft, cellTop, 
						cellLeft + cellWidth, cellTop + cellHeight,
						selectedPaint);
			}
			
			// visually highlight cell under the finger (to cope with touch screen
			// imprecision)
			if (touchedCell != null) {
				cellLeft = touchedCell.getColumnIndex() * cellWidth;
				cellTop = touchedCell.getRowIndex() * cellHeight;
				canvas.drawRect(
						cellLeft, 0,
						cellLeft + cellWidth, height,
						touchedPaint);
				canvas.drawRect(
						0, cellTop,
						width, cellTop + cellHeight,
						touchedPaint);
			}

		}
		
		// draw vertical lines
		for (int c=0; c < 9; c++) {
			int x = c * cellWidth;
			if (c % 3 == 0) {
				canvas.drawRect(x-1, 0, x+1, height, linePaint);
			} else {
				canvas.drawLine(x, 0, x, height, linePaint);
			}
		}
		
		// draw horizontal lines
		for (int r=0; r < 9; r++) {
			int y = r * cellHeight;
			if (r % 3 == 0) {
				canvas.drawRect(0, y-1, width, y+1, linePaint);
			} else {
				canvas.drawLine(0, y, width, y, linePaint);
			}
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		
		if (!readonly) {
			int x = (int)event.getX();
			int y = (int)event.getY();
			
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				touchedCell = getCellAtPoint(x, y);
				break;
			case MotionEvent.ACTION_UP:
				selectedCell = getCellAtPoint(x, y);
				
				boolean selectNumberShowed = false;
				if (selectedCell != null) {
					if (onCellTapListener != null) {
						onCellTapListener.onCellTap(selectedCell);
					}
					ensureEditCellDialog();
					if (selectedCell.getEditable() && editCellDialog != null) {
						editCellDialog.updateNumber(selectedCell.getValue());
						editCellDialog.updateNote(getNoteNumbers(selectedCell.getNote()));
						editCellDialog.getDialog().show();
						selectNumberShowed = true;
					}
				}
				
				// If select number dialog wasn't showed, clear touched cell highlight, if dialog
				// is visible, highlight will be cleared after dialog is dismissed.
				if (!selectNumberShowed) {
					touchedCell = null;
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				touchedCell = null;
				break;
			}
			invalidate();
		}
		
		return !readonly;
	}
	
	// TODO: do I really need this?
	@Override
	public boolean onTrackballEvent(MotionEvent event) {
    	// Actually, just let these come through as D-pad events.
    	return false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (!readonly) {
			switch (keyCode) {
				case KeyEvent.KEYCODE_DPAD_UP:
					return moveCellSelection(0, -1);
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					return moveCellSelection(1, 0);
				case KeyEvent.KEYCODE_DPAD_DOWN:
					return moveCellSelection(0, 1);
				case KeyEvent.KEYCODE_DPAD_LEFT:
					return moveCellSelection(-1, 0);
				case KeyEvent.KEYCODE_0:
				case KeyEvent.KEYCODE_SPACE:
				case KeyEvent.KEYCODE_DEL:
					// clear value in selected cell
					if (selectedCell != null) {
						setCellValue(selectedCell, 0);
						moveCellSelectionRight();
					}
					return true;
			}
			
			if (keyCode >= KeyEvent.KEYCODE_1 && keyCode <= KeyEvent.KEYCODE_9) {
				// enter request number in cell
				int selectedNumber = keyCode - KeyEvent.KEYCODE_0;
				setCellValue(selectedCell, selectedNumber);
				moveCellSelectionRight();
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Occurs when user selects number in EditCellDialog.
	 */
    private OnNumberEditListener onNumberEditListener = new OnNumberEditListener() {
		@Override
		public boolean onNumberEdit(int number) {
    		SudokuCell selectedCell = getSelectedCell();
    		if (number != -1) {
                // set cell number selected by user
				setCellValue(selectedCell, number);
				touchedCell = null;
				invalidate();
    		}
			return true;
		}
	};
	
	/**
	 * Occurs when user edits note in EditCellDialog
	 */
	private OnNoteEditListener onNoteEditListener = new OnNoteEditListener() {
		@Override
		public boolean onNoteEdit(Integer[] numbers) {
			SudokuCell selectedCell = getSelectedCell();
			if (selectedCell != null) {
				setCellNote(selectedCell, setNoteNumbers(numbers));
				touchedCell = null;
				invalidate();
			}
			return true;
		}
	};
	
	private void setCellValue(SudokuCell cell, int value) {
		if (game != null) {
			game.setCellValue(cell, value);
		} else {
			cell.setValue(value);
		}
	}
	
	private void setCellNote(SudokuCell cell, String note) {
		if (game != null) {
			game.setCellNote(cell, note);
		} else {
			cell.setNote(note);
		}
	}
	
	
	/**
	 * Moves selected cell by one cell to the right. If edge is reached, selection
	 * skips on beginning of another line. 
	 */
	private void moveCellSelectionRight() {
		if (!moveCellSelection(1, 0)) {
			int selRow = selectedCell.getRowIndex();
			selRow++;
			if (!moveCellSelectionTo(selRow, 0)) {
				moveCellSelectionTo(0, 0);
			}
		}
	}
	
	/**
	 * Moves selected by vx cells right and vy cells down. vx and vy can be negative. Returns true,
	 * if new cell is selected.
	 * 
	 * @param vx Horizontal offset, by which move selected cell.
	 * @param vy Vertical offset, by which move selected cell.
	 */
	private boolean moveCellSelection(int vx, int vy) {
		int newRow = 0;
		int newCol = 0;
		
		if (selectedCell != null) {
			newRow = selectedCell.getRowIndex() + vy;
			newCol = selectedCell.getColumnIndex() + vx;
		}
		
		return moveCellSelectionTo(newRow, newCol);
	}
	
	
	/**
	 * Moves selection to the cell given by row and column index.
	 * @param row Row index of cell which should be selected.
	 * @param col Columnd index of cell which should be selected.
	 * @return True, if cell was successfuly selected.
	 */
	private boolean moveCellSelectionTo(int row, int col) {
		if(col >= 0 && col < SudokuCellCollection.SUDOKU_SIZE 
				&& row >= 0 && row < SudokuCellCollection.SUDOKU_SIZE) {
			selectedCell = cells.getCell(row, col);
			postInvalidate();
			return true;
		}
		
		return false;
	}
	
	/**
	 * Get cell at given screen coordinates. Returns null if no cell is found.
	 * @param x
	 * @param y
	 * @return
	 */
	private SudokuCell getCellAtPoint(int x, int y) {
		// TODO: this is not nice, col/row vs x/y
		
		int row = y / cellHeight;
		int col = x / cellWidth;
		
		if(col >= 0 && col < SudokuCellCollection.SUDOKU_SIZE 
				&& row >= 0 && row < SudokuCellCollection.SUDOKU_SIZE) {
			return cells.getCell(row, col);
		} else {
			return null;
		}
	}
	
	/**
	 * Returns content of note as array of numbers. Note is expected to be
	 * in format "n,n,n".
	 * 
	 * @return
	 */
	private Integer[] getNoteNumbers(String note) {
		if (note == null || note.equals(""))
			return null;
		
		String[] numberStrings = note.split(",");
		Integer[] numbers = new Integer[numberStrings.length];
		for (int i=0; i<numberStrings.length; i++) {
			numbers[i] = Integer.parseInt(numberStrings[i]);
		}
		
		return numbers;
	}
	
	/**
	 * Creates content of note from array of numbers. Note will be stored
	 * in "n,n,n" format.
	 * 
	 * TODO: find better name for this method
	 * 
	 * @param numbers
	 */
	private String setNoteNumbers(Integer[] numbers) {
		StringBuffer sb = new StringBuffer();
		
		for (Integer number : numbers) {
			sb.append(number).append(",");
		}
		
		return sb.toString();
	}
	
	
	public interface OnCellTapListener
	{
		/**
		 * Called when a cell is tapped (by finger).
		 * @param cell
		 * @return
		 */
		void onCellTap(SudokuCell cell);
	}



}