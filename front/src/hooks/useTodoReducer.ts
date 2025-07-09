import { useReducer } from "react";
import type { TodoInterface } from "../type/Todo.interface";
import type { TodoAction } from "../type/TodoActions";

interface TodoState {
  todos: TodoInterface[];
  loading: boolean;
  previousState: TodoInterface[]; // for rollback
}

const initialState: TodoState = {
  todos: [],
  loading: true,
  previousState: [],
};

function todoReducer(state: TodoState, action: TodoAction): TodoState {
  switch (action.type) {
    case "SET_TODOS":
      return {
        ...state,
        todos: action.payload,
        loading: false,
        previousState: state.todos, // Save for potential rollback
      };

    case "ADD_TODO":
      return {
        ...state,
        todos: [...state.todos, action.payload],
        previousState: state.todos,
      };

    case "DELETE_TODO":
      return {
        ...state,
        todos: state.todos.filter((todo) => todo.id !== action.payload.id),
        previousState: state.todos,
      };

    case "UPDATE_TODO":
      return {
        ...state,
        todos: state.todos.map((todo) =>
          todo.id === action.payload.id ? action.payload : todo
        ),
      };

    case "MOVE_TODO":
      return {
        ...state,
        todos: state.todos.map((todo) =>
          todo.id === action.payload.id
            ? {
                ...todo,
                status: action.payload.status,
                order: action.payload.order,
              }
            : todo
        ),
        previousState: state.todos,
      };

    case "SET_LOADING":
      return {
        ...state,
        loading: action.payload,
      };

    case "REVERT_STATE":
      return {
        ...state,
        todos: state.previousState,
      };

    default:
      return state;
  }
}

export function useTodoReducer() {
  return useReducer(todoReducer, initialState);
}
