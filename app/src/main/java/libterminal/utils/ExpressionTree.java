package libterminal.utils;

import java.util.Stack;

public final class ExpressionTree {

	private final ExpressionNode expressionRootNode;

	public ExpressionTree(final String expression) throws IllegalArgumentException {
		this.expressionRootNode = buildExpressionTree(expression);
	}

	private static ExpressionNode buildExpressionTree(final String expression) throws IllegalArgumentException {
		final Stack<ExpressionNode> stack = new Stack<>();
		final int[] exp = Utils.fromInfixToPostfix(expression);
		for (final int value : exp) {
			if (value == Utils.AND_INT_VALUE || value == Utils.OR_INT_VALUE) {
				stack.push(new ExpressionNode(value, stack.pop(), stack.pop()));
			} else {
				stack.push(new ExpressionNode(value));
			}
		}
		return stack.pop();
	}

	public boolean evaluateExpressionTree(final boolean[] touchedNodes) {
		return evaluateExpressionTree(expressionRootNode, touchedNodes);
	}

	private boolean evaluateExpressionTree(final ExpressionNode node, final boolean[] touchedNodes) {
		if (node.isLeaf()) {
			final int nodeId = node.getValue();
			return touchedNodes[nodeId];
		} else {
			switch (node.getValue()) {
			case Utils.AND_INT_VALUE: {
				return evaluateExpressionTree(node.getLeft(), touchedNodes) && evaluateExpressionTree(node.getRight(), touchedNodes);
			}
			case Utils.OR_INT_VALUE: {
				return evaluateExpressionTree(node.getLeft(), touchedNodes) || evaluateExpressionTree(node.getRight(), touchedNodes);
			}
			default: {
				return false;
			}
			}
		}
	}

	private static class ExpressionNode {

		private final int value;
		private final ExpressionNode right;
		private final ExpressionNode left;

		public ExpressionNode(final int value) {
			this(value, null, null);
		}

		public ExpressionNode(final int value, final ExpressionNode right, final ExpressionNode left) {
			this.value = value;
			this.right = right;
			this.left = left;
		}

		public int getValue() {
			return value;
		}

		public ExpressionNode getRight() {
			return right;
		}

		public ExpressionNode getLeft() {
			return left;
		}

		public boolean isLeaf() {
			return right == null && left == null;
		}

	}

}
