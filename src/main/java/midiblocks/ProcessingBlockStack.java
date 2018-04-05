package midiblocks;
import java.util.LinkedList;
import java.util.Stack;

import processingblocks.ProcessingBlock;

/**
 * This class is a data structure that is used the processing block states
 * to allow for 'undo' operations.
 * @author Lisa Liu-Thorrold
 *
 */
public class ProcessingBlockStack extends Stack<LinkedList<ProcessingBlock>>{
	
	private static final long serialVersionUID = 1079017799488293026L;
	private LinkedList<ProcessingBlock> firstState;
	
	public ProcessingBlockStack(MidiModel model) {
		super();
		firstState = new LinkedList<>(
				model.getProcessingBlocks());
	}

	/**
	 * This method adds a processing block list state to the stack.
	 * If the size is greater than 15, we remove the oldest action (the last
	 * item in the stack
	 */
	@Override
	public LinkedList<ProcessingBlock> push(LinkedList<ProcessingBlock> list) {
		while(this.size() >= 15) {
			this.remove(0);
			firstState = elementAt(0);
		}

		return super.push(list);
	}
	
	/**
	 * This method removes a processing block list state off the stack.
	 * This is invoked when an 'undo' operation is called.
	 * @return The last processing block list state that was added to the stack
	 */
	@Override
	public LinkedList<ProcessingBlock> pop() {
		LinkedList<ProcessingBlock> state;
        int     len = size();
        
        if (len >= 1) {
        	   state = peek();
               removeElementAt(len - 1);
               return state;

        } else {
            return firstState;
        }

	}
}
