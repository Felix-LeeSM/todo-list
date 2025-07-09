const CHARSET = "0123456789abcdefghijklmnopqrstuvwxyz";
const MAX_LENGTH = 256;

/**
 * lexicographical order를 유지하는 문자열 생성 함수
 * @param left 왼쪽 경계 문자열 (선택적)
 * @param right 오른쪽 경계 문자열 (선택적)
 * @returns 사이에 삽입될 문자열
 */
export const generateOrderedString = (
  left?: string,
  right?: string
): string => {
  if (!left && !right) {
    return "i";
  }
  if (left && !right) {
    return generateAfter(left);
  }
  if (left && right) {
    return generateBetween(left, right);
  }
  if (!left && right) {
    return generateBefore(right);
  }
  throw new Error("Invalid input");
};

/**
 * 중간값에 해당하는 문자 생성 (약간의 임의성 포함)
 */
const generateMiddleChar = (): string => {
  const midIndex = Math.floor(CHARSET.length / 2);
  const randomOffset = Math.floor(Math.random() * 5) - 2; // -2 ~ 2
  const targetIndex = Math.max(
    1, // ✨ 수정: '0'을 반환하지 않도록 최소값을 1로 설정
    Math.min(CHARSET.length - 1, midIndex + randomOffset)
  );
  return CHARSET[targetIndex];
};

/**
 * 주어진 문자열 뒤에 올 문자열 생성
 */
const generateAfter = (str: string): string => {
  if (str.length >= MAX_LENGTH) {
    throw new Error("String too long");
  }
  const lastChar = str[str.length - 1];
  const lastCharIndex = CHARSET.indexOf(lastChar);
  const maxCharIndex = CHARSET.length - 1;

  if (lastCharIndex < maxCharIndex) {
    const nextCharIndex = lastCharIndex + 1;
    if (nextCharIndex - lastCharIndex > 1) {
      const midIndex = Math.floor((lastCharIndex + nextCharIndex) / 2);
      return str.slice(0, -1) + CHARSET[midIndex];
    }
    return str + generateMiddleChar();
  }
  return str + generateMiddleChar();
};

/**
 * ✨ [핵심 수정] 주어진 문자열 앞에 올 문자열 생성
 */
const generateBefore = (str: string): string => {
  if (str.length >= MAX_LENGTH) {
    throw new Error("String too long");
  }

  const minChar = CHARSET[0];
  let prefix = "";
  let i = 0;

  // '0'으로 시작하는 부분을 prefix로 추출
  while (i < str.length && str[i] === minChar) {
    prefix += minChar;
    i++;
  }

  // 문자열 전체가 '0'으로만 구성된 경우 (이론상 발생하면 안 되지만 방어 코드)
  if (i === str.length) {
    throw new Error(
      "Cannot generate a key before an all-minimum-character key."
    );
  }

  // '0'이 아닌 첫 문자를 찾음
  const charAtIndex = str[i];
  const charIndex = CHARSET.indexOf(charAtIndex);

  // '0'과 해당 문자 사이의 중간값을 찾음
  const midIndex = Math.floor(charIndex / 2);

  // 만약 중간값이 0이면 ('1' 앞에 삽입하는 경우), '0'을 하나 더 추가하고 중간 문자를 붙임
  if (midIndex === 0) {
    return prefix + minChar + generateMiddleChar();
  } else {
    // 그 외의 경우, 계산된 중간 문자를 사용
    return prefix + CHARSET[midIndex];
  }
};

/**
 * ✨ [핵심 수정] 두 문자열 사이의 중간값 생성
 */
const generateBetween = (left: string, right: string): string => {
  if (left >= right) {
    throw new Error("Left must be less than right");
  }

  let result = "";
  let i = 0;

  while (true) {
    const leftChar = i < left.length ? left[i] : CHARSET[0];
    // ✨ 수정: 오른쪽 경계가 짧아질 때 'z' 대신 가상의 최대값으로 처리
    const rightChar = i < right.length ? right[i] : CHARSET[CHARSET.length - 1];

    const leftIndex = CHARSET.indexOf(leftChar);
    const rightIndex = CHARSET.indexOf(rightChar);

    if (leftIndex === rightIndex) {
      result += leftChar;
      i++;
      continue;
    }

    // ✨ 수정: 왼쪽 문자열이 오른쪽 문자열의 접두사인 경우 (e.g., left="a", right="az")
    if (i >= left.length && i < right.length) {
      const midIndex = Math.floor(rightIndex / 2);
      // '0'을 피하기 위해 midIndex가 0이면 generateMiddleChar() 사용
      if (midIndex > 0) {
        return result + CHARSET[midIndex];
      } else {
        return result + CHARSET[0] + generateMiddleChar();
      }
    }

    if (rightIndex - leftIndex > 1) {
      const midIndex = Math.floor((leftIndex + rightIndex) / 2);
      return result + CHARSET[midIndex];
    } else {
      // 문자가 인접한 경우 (e.g., 'a', 'b')
      result += leftChar;
      left = i + 1 < left.length ? left.slice(i + 1) : "";
      right = ""; // 오른쪽 경계는 무시하고 왼쪽 기준 다음으로 생성
      i = 0; // 인덱스 초기화
    }
  }
};
