// src/pages/Auth/OAuth2Success.jsx
import { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

// ✅ axios 인스턴스를 직접 사용(기존 인터셉터 활용)
import apiClient from '@/api/axios';

const OAuth2Success = () => {
  const nav = useNavigate();
  const loc = useLocation();
  const [msg, setMsg] = useState('로그인 처리 중...');

  useEffect(() => {
    (async () => {
      try {
        // 0) 카카오에서 에러가 온 경우 처리
        const sp = new URLSearchParams(loc.search);
        const err = sp.get('error') || sp.get('error_description');
        if (err) {
          console.error('OAuth error:', err);
          setMsg('로그인에 실패했어요. 잠시 후 로그인 페이지로 이동합니다.');
          setTimeout(() => nav('/login', { replace: true }), 1200);
          return;
        }

        // 1) 서버가 쿼리/해시로 토큰을 넘겨준 경우 저장 (axios는 localStorage의 'token'을 사용함)
        const hash = new URLSearchParams(loc.hash.replace(/^#/, ''));
        const tokenFromQuery = sp.get('token') || hash.get('token');
        if (tokenFromQuery) {
          localStorage.setItem('token', tokenFromQuery);               // ✅ 기존 axios 인터셉터 호환
          apiClient.defaults.headers.Authorization = `Bearer ${tokenFromQuery}`; // (선반영) 첫 요청 안정화
          // URL 정리(토큰 제거)
          const cleanUrl = window.location.pathname;
          window.history.replaceState({}, '', cleanUrl);
        }

        // 2) 세션/토큰 확인: 서버가 httpOnly 쿠키만 심었더라도 통과됨
        await apiClient.get('/auth/me');

        // 3) 성공 → 메인으로
        nav('/ingredients', { replace: true });
      } catch (e) {
        console.error(e);
        // 4) 401 등 실패 → 로그인으로
        setMsg('세션 확인에 실패했어요. 잠시 후 로그인 페이지로 이동합니다.');
        setTimeout(() => nav('/login', { replace: true }), 1200);
      }
    })();
  }, [loc.search, loc.hash, nav]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-gradient-to-br from-purple-50 to-white">
      <div className="rounded-2xl bg-white p-8 shadow-lg">
        <p className="text-gray-700">{msg}</p>
      </div>
    </div>
  );
};

export default OAuth2Success;
