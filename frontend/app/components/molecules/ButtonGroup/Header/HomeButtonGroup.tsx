"use client";
import { AiFillHome } from 'react-icons/ai';
import HeaderButtonGroup from '../../HeaderButtonGroup';
import Link from 'next/link';
import styled from '@emotion/styled';
import { useDate } from '@/app/store/date';

interface HomeButtonGroupProps {
  onClick?: () => void; 
}

const HomeButtonGroup = ({ onClick }: HomeButtonGroupProps) => {
  const {date} = useDate();
    return (
    <div
      onClick={(e) => {
        e.stopPropagation(); // 이벤트 전파 방지 (메뉴 닫힘 이벤트 방지)
        if (onClick) {
          onClick();
        }
      }}
    >
      <StyledLink href={`/main/${date}`}>
        <HeaderButtonGroup
          icon={<AiFillHome />}
          label="홈"
        />
      </StyledLink>
    </div>
  );
};

const StyledLink = styled(Link)`
  text-decoration: none;
`

export default HomeButtonGroup;
