import { PageContainer } from '@ant-design/pro-components';
import React, { useEffect, useState } from 'react';
import ReactECharts from 'echarts-for-react';
import { listTopInvokeInterfaceInfoUsingGet } from '@/services/xiaohang-backend/analysisController';
import './index.less';

const InterfaceAnalysis: React.FC = () => {
  const [data, setData] = useState<API.InterfaceInfoVO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    listTopInvokeInterfaceInfoUsingGet().then((res) => {
      if (res.data) setData(res.data);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, []);

  const chartData = data
    .filter((item) => item.totalNum !== undefined)
    .sort((a, b) => (b.totalNum ?? 0) - (a.totalNum ?? 0))
    .slice(0, 8)
    .map((item) => ({ name: item.name, value: item.totalNum ?? 0 }));

  const totalInvoke = chartData.reduce((sum, d) => sum + d.value, 0);

  const option = {
    backgroundColor: 'transparent',
    title: {
      text: 'API Invocation Analysis',
      subtext: `Top 8 by total calls · ${totalInvoke.toLocaleString()} total`,
      left: 'center',
      textStyle: { color: '#E2EAF4', fontSize: 18, fontWeight: 800 },
      subtextStyle: { color: '#8892A4', fontSize: 13 },
    },
    tooltip: {
      trigger: 'item',
      backgroundColor: '#0C1929',
      borderColor: 'rgba(0, 212, 170, 0.2)',
      borderWidth: 1,
      textStyle: { color: '#E2EAF4', fontFamily: "'Cascadia Code', monospace" },
      formatter: (params: any) => {
        return `<span style="color:#00D4AA;font-weight:700">${params.name}</span><br/>` +
          `Calls: <span style="color:#00D4AA;font-weight:700">${params.value.toLocaleString()}</span><br/>` +
          `Share: <span style="color:#00A3FF">${params.percent}%</span>`;
      },
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      top: 'middle',
      textStyle: { color: '#8892A4', fontSize: 12 },
      icon: 'circle',
      itemWidth: 8,
      itemGap: 16,
    },
    series: [
      {
        name: 'Invocations',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['60%', '55%'],
        avoidLabelOverlap: true,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#060B14',
          borderWidth: 2,
        },
        label: { show: false },
        emphasis: {
          label: { show: true, fontSize: 14, fontWeight: 700, color: '#E2EAF4' },
          itemStyle: { shadowBlur: 20, shadowColor: 'rgba(0, 212, 170, 0.3)' },
          scaleSize: 8,
        },
        data: chartData,
        color: ['#00D4AA', '#00A3FF', '#00BFA0', '#5CB85C', '#F0AD4E', '#5BC0DE', '#00D4AA88', '#00A3FF88'],
      },
    ],
  };

  return (
    <PageContainer>
      <div className="analysis-page">
        <div className="analysis-card">
          <div className="analysis-header">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" />
            </svg>
            <h3>Call Volume Distribution</h3>
          </div>
          {chartData.length === 0 && !loading ? (
            <div className="analysis-empty">
              <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" opacity="0.3">
                <rect x="3" y="3" width="18" height="18" rx="2" /><path d="M3 9h18M9 21V9" />
              </svg>
              <p>No invocation data available yet.</p>
            </div>
          ) : (
            <ReactECharts showLoading={loading} option={option} className="analysis-chart" />
          )}
        </div>
      </div>
    </PageContainer>
  );
};

export default InterfaceAnalysis;
