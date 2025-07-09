const CHARACTERS = "0123456789abcdefghijklmnopqrstuvwxyz";
const MIDDLE_CHAR = "i";
const LAST_CHAR_INDEX = CHARACTERS.length - 1;

export const generateOrder = (prev?: string, next?: string): string => {
  if (!prev && !next) return MIDDLE_CHAR;

  const normalizedPrev = prev || "";
  const normalizedNext = next || "";

  return generateOrderBetween(normalizedPrev, normalizedNext);
};

const generateOrderBetween = (prev: string, next: string): string => {
  let position = 0;
  let result = "";

  while (true) {
    const prevCharIndex = getCharIndex(prev, position);
    const nextCharIndex = getCharIndex(next, position);

    if (prevCharIndex === nextCharIndex) {
      result += CHARACTERS[prevCharIndex];
      position++;
      continue;
    }

    if (canInsertBetween(prevCharIndex, nextCharIndex)) {
      const middleIndex = calculateMiddleIndex(prevCharIndex, nextCharIndex);
      return result + CHARACTERS[middleIndex];
    }

    return extendWithMiddleChar(result, prev, prevCharIndex, position);
  }
};

const getCharIndex = (str: string, position: number): number => {
  if (position >= str.length) {
    return str ? LAST_CHAR_INDEX + 1 : 0; // Past end: use boundary value
  }

  const charIndex = CHARACTERS.indexOf(str[position]);
  if (charIndex === -1) {
    throw new Error(
      `Invalid character '${str[position]}' at position ${position}`
    );
  }

  return charIndex;
};

const canInsertBetween = (prevIndex: number, nextIndex: number): boolean => {
  return nextIndex - prevIndex > 1;
};

const calculateMiddleIndex = (prevIndex: number, nextIndex: number): number => {
  return Math.floor((prevIndex + nextIndex) / 2);
};

const extendWithMiddleChar = (
  currentResult: string,
  prev: string,
  prevCharIndex: number,
  startPosition: number
): string => {
  let result = currentResult + CHARACTERS[prevCharIndex];
  let position = startPosition + 1;

  while (position < prev.length && isLastCharacter(prev[position])) {
    result += CHARACTERS[LAST_CHAR_INDEX];
    position++;
  }

  return result + MIDDLE_CHAR;
};

const isLastCharacter = (char: string): boolean => {
  return CHARACTERS.indexOf(char) === LAST_CHAR_INDEX;
};

export const isValidOrderString = (str: string): boolean => {
  return str.split("").every((char) => CHARACTERS.includes(char));
};

export const compareOrderStrings = (a: string, b: string): number => {
  return a.localeCompare(b);
};

export const generateOrderSequence = (count: number): string[] => {
  const result: string[] = [];
  let current = "";

  for (let i = 0; i < count; i++) {
    const next = generateOrder(current);
    result.push(next);
    current = next;
  }

  return result;
};

export const orderUtils = {
  generateOrder,
  isValidOrderString,
  compareOrderStrings,
  generateOrderSequence,
  CHARACTERS,
  MIDDLE_CHAR,
};

export type OrderString = string;
export type OrderPosition = "start" | "middle" | "end";

export const createOrderAtPosition = (position: OrderPosition): OrderString => {
  switch (position) {
    case "start":
      return generateOrder(undefined, MIDDLE_CHAR);
    case "middle":
      return MIDDLE_CHAR;
    case "end":
      return generateOrder(MIDDLE_CHAR, undefined);
    default:
      throw new Error(`Invalid position: ${position}`);
  }
};
