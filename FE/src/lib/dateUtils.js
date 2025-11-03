/**
 * 마감 시간까지 남은 시간을 계산하여 표시 형식으로 반환
 * @param {string} deadlineDate - ISO 8601 형식의 날짜 문자열 (예: "2025-11-03T18:00:00")
 * @returns {string} - "3시간 남음", "1일 남음", "마감" 등
 */
export const getTimeRemaining = (deadlineDate) => {
  if (!deadlineDate) return '날짜 미정';

  const now = new Date();
  const deadline = new Date(deadlineDate);
  const diffMs = deadline - now;

  // 이미 마감된 경우
  if (diffMs <= 0) {
    return '마감';
  }

  const diffMinutes = Math.floor(diffMs / (1000 * 60));
  const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  // 1분 미만
  if (diffMinutes < 1) {
    return '곧 마감';
  }

  // 1시간 미만
  if (diffHours < 1) {
    return `${diffMinutes}분 남음`;
  }

  // 24시간 미만
  if (diffDays < 1) {
    return `${diffHours}시간 남음`;
  }

  // 1일 이상
  if (diffDays === 1) {
    return '1일 남음';
  }

  return `${diffDays}일 남음`;
};

/**
 * 날짜를 "MM/DD (요일) HH:MM" 형식으로 표시
 * @param {string} dateString - ISO 8601 형식의 날짜 문자열
 * @returns {string}
 */
export const formatDateTime = (dateString) => {
  if (!dateString) return '';

  const date = new Date(dateString);
  const month = (date.getMonth() + 1).toString();
  const day = date.getDate().toString();
  const hours = date.getHours().toString().padStart(2, '0');
  const minutes = date.getMinutes().toString().padStart(2, '0');

  const weekdays = ['일', '월', '화', '수', '목', '금', '토'];
  const weekday = weekdays[date.getDay()];

  return `${month}/${day} (${weekday}) ${hours}:${minutes}`;
};
