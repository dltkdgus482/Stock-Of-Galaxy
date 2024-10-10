import React, { useRef, useEffect, useState } from 'react';
import styled from '@emotion/styled';
import * as d3 from 'd3';
import cloud from 'd3-cloud';

const TooltipContainer = styled.div<{ x: number; y: number }>`
  position: fixed;
  top: ${(props) => props.y}px;
  left: ${(props) => props.x}px;
  background: rgba(0, 0, 0, 0.7);
  color: white;
  padding: 5px 10px;
  border-radius: 4px;
  pointer-events: none;
  font-size: 12px;
  white-space: nowrap;
  z-index: 1000;
`;

interface TooltipProps {
  x: number;
  y: number;
  text: string;
}

const Tooltip: React.FC<TooltipProps> = ({ x, y, text }) => {
  return <TooltipContainer x={x} y={y}>{text}</TooltipContainer>;
};

// 워드 클라우드 컨테이너 스타일
const WordCloudContainer = styled.div`
  width: 100%;
  height: 100%;
  position: relative;
  background-color: rgba(255, 255, 255, 0.8);
`;

// 워드 인터페이스 정의
interface Word {
  text: string;
  value: number;
}

// WordCloudComponent Props 인터페이스
interface WordCloudProps {
  data: Word[];
  width: number; // 가로 사이즈
  height: number; // 세로 사이즈
}

const WordCloudComponent: React.FC<WordCloudProps> = ({ data, width, height }) => {
  const svgRef = useRef<SVGSVGElement>(null);
  const [words, setWords] = useState<{ text: string; x: number; y: number; size: number; rotate: number }[]>([]);
  const [tooltip, setTooltip] = useState<{ x: number; y: number; text: string } | null>(null);

  useEffect(() => {
    const layout = cloud<Word>()
      .size([width+100, height+100]) // props로 전달된 가로 및 세로 사이즈 설정
      .words(data.map((d) => ({
        text: d.text,
        size: d.value * 10,
      })))
      .padding(5)
      .rotate(() => (Math.random() > 0.5 ? 90 : 0))
      .font('Nanum Gothic')
      .fontSize((d) => d.size)
      .on('end', (words) => {
        setWords(words);
      });

    layout.start();
  }, [data, width, height]);

  const handleMouseEnter = (event: React.MouseEvent<SVGTextElement, MouseEvent>, word: typeof words[0]) => {
    const { clientX, clientY } = event;
    setTooltip({
      x: clientX + 10,
      y: clientY + 10,
      text: word.text,
    });
  };

  const handleMouseLeave = () => {
    setTooltip(null);
  };

  return (
    <WordCloudContainer>
      <svg ref={svgRef} width={width+100} height={height+100}>
        <g transform={`translate(${(width+20) / 2},${(height+100) / 2})`}>
          {words.map((word, index) => (
            <text
              key={index}
              fontSize={word.size}
              fontFamily="Nanum Gothic"  // 폰트를 적용
              fontWeight="bold"
              fill="#29088A"
              textAnchor="middle"
              transform={`translate(${word.x},${word.y}) rotate(${word.rotate})`}
              style={{ userSelect: 'none', cursor: 'pointer' }}
              onMouseEnter={(e) => handleMouseEnter(e, word)}
              onMouseLeave={handleMouseLeave}
            >
              {word.text}
            </text>
          ))}
        </g>
      </svg>
      {tooltip && <Tooltip x={tooltip.x} y={tooltip.y} text={tooltip.text} />}
    </WordCloudContainer>
  );
};

// React.memo로 감싸서 리렌더링 방지
export default React.memo(WordCloudComponent);
