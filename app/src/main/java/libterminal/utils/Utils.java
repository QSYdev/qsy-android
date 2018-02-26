package libterminal.utils;

import libterminal.lib.protocol.QSYPacket;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

public final class Utils {

	public static final int AND_INT_VALUE = -1;
	public static final int OR_INT_VALUE = -2;
	private static final int OPEN_PARENTHESIS_INT_VALUE = -3;
	private static final int CLOSE_PARENTHESIS_INT_VALUE = -4;

	private Utils() {
	}

	public static int[] fromInfixToPostfix(final String expression) throws IllegalArgumentException {
		final Stack<Integer> stack = new Stack<>();
		final LinkedList<Integer> queue = new LinkedList<>();

		final int[] exp = fromStringToIntArray(expression);

		byte index = 0;
		while (index < exp.length) {
			int c = exp[index++];
			if (c >= QSYPacket.MIN_ID_SIZE && c <= QSYPacket.MAX_ID_SIZE) {
				queue.add(c);
			} else if (c == AND_INT_VALUE || c == OR_INT_VALUE) {
				while (!stack.isEmpty() && getPrior(c) <= getPrior(stack.peek())) {
					queue.add(stack.pop());
				}
				stack.push(c);
			} else if (c == OPEN_PARENTHESIS_INT_VALUE) {
				stack.push(c);
			} else if (c == CLOSE_PARENTHESIS_INT_VALUE) {
				int elem = 0;
				while (!stack.isEmpty() && (elem = stack.pop()) != OPEN_PARENTHESIS_INT_VALUE) {
					queue.add(elem);
				}
				if (elem != OPEN_PARENTHESIS_INT_VALUE) {
					throw new IllegalArgumentException("<< QSY_UTILS_ERROR >> La expresion '" + expression + "' es invalida.");
				}
			} else {
				throw new IllegalArgumentException("<< QSY_UTILS_ERROR >> La expresion '" + expression + "' es invalida.");
			}
		}
		while (!stack.isEmpty()) {
			int elem;
			if ((elem = stack.pop()) != OPEN_PARENTHESIS_INT_VALUE) {
				queue.add(elem);
			} else {
				throw new IllegalArgumentException("<< QSY_UTILS_ERROR >> La expresion '" + expression + "' es invalida.");
			}
		}

		return toIntArray(queue);
	}

	private static int getPrior(final int c) {
		switch (c) {
		case OR_INT_VALUE: {
			return 1;
		}
		case AND_INT_VALUE: {
			return 2;
		}
		case OPEN_PARENTHESIS_INT_VALUE: {
			return 0;
		}
		default: {
			return -1;
		}
		}
	}

	private static int[] fromStringToIntArray(final String expression) throws IllegalArgumentException {
		final LinkedList<Integer> queue = new LinkedList<>();
		final LinkedList<Integer> number = new LinkedList<>();
		byte index = 0;
		while (index < expression.length()) {
			char value = expression.charAt(index++);
			if (value >= '0' && value <= '9') {
				number.add(value - 48);
			} else {
				if (!number.isEmpty()) {
					final int num = fromIntBufferToInt(number);
					if (num >= QSYPacket.MIN_ID_SIZE && num <= QSYPacket.MAX_ID_SIZE) {
						queue.add(num);
						number.clear();
					} else {
						throw new IllegalArgumentException("<< QSY_UTILS_ERROR >> La expresion '" + expression + "' es invalida.");
					}
				}
				if (value == '&') {
					queue.add(AND_INT_VALUE);
				} else if (value == '|') {
					queue.add(OR_INT_VALUE);
				} else if (value == '(') {
					queue.add(OPEN_PARENTHESIS_INT_VALUE);
				} else if (value == ')') {
					queue.add(CLOSE_PARENTHESIS_INT_VALUE);
				} else if (value == ' ') {

				} else {
					throw new IllegalArgumentException("<< QSY_UTILS_ERROR >> La expresion '" + expression + "' es invalida.");
				}
			}
		}
		if (!number.isEmpty()) {
			final int num = fromIntBufferToInt(number);
			if (num >= QSYPacket.MIN_ID_SIZE && num <= QSYPacket.MAX_ID_SIZE) {
				queue.add(num);
				number.clear();
			}
		}

		return toIntArray(queue);
	}

	private static Integer fromIntBufferToInt(final LinkedList<Integer> number) {
		int result = 0;
		byte pow = 0;
		final Iterator<Integer> iterator = number.descendingIterator();
		while (iterator.hasNext()) {
			result += Math.pow(10, pow++) * iterator.next();
		}
		return result;
	}

	private static int[] toIntArray(final LinkedList<Integer> list) {
		final int[] result = new int[list.size()];
		byte i = 0;
		for (final int c : list) {
			result[i++] = c;
		}

		return result;
	}
}
