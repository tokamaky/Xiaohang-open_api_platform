import { PageContainer } from '@ant-design/pro-components';
import React, { useEffect, useState } from 'react';
import ReactECharts from 'echarts-for-react';
import { Radio, Card, Space } from 'antd';
import {
  UserOutlined,
  AppstoreOutlined,
  TeamOutlined,
} from '@ant-design/icons';
import { listAllInvokeInterfaceInfoUsingGet, listCurrentUserInvokeInterfaceInfoUsingGet } from '@/services/xiaohang-backend/analysisController';
import { useModel } from '@umijs/max';
import './index.less';

type ViewMode = 'mine' | 'all';

const InterfaceAnalysis: React.FC = () => {
  const { initialState } = useModel('@@initialState');
  const isAdmin = initialState?.loginUser?.userRole === 'admin';

  const [viewMode, setViewMode] = useState<ViewMode>('mine');
  const [data, setData] = useState<API.InterfaceInfoVO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    setLoading(true);
    const request = viewMode === 'mine'
      ? listCurrentUserInvokeInterfaceInfoUsingGet()
      : listAllInvokeInterfaceInfoUsingGet();
    request.then((res) => {
      if (res.data) setData(res.data);
      setLoading(false);
    }).catch(() => setLoading(false));
  }, [viewMode]);

  const chartData = data
    .filter((item) => item.totalNum !== undefined)
    .sort((a, b) => (b.totalNum ?? 0) - (a.totalNum ?? 0))
    .slice(0, 20)
    .map((item) => ({ name: item.name, value: item.totalNum ?? 0 }));

  const totalInvoke = chartData.reduce((sum, d) => sum + d.value, 0);

  const chartTitle = viewMode === 'mine'
    ? 'My API Calls'
    : 'Platform Total Calls';
  const chartSubtext = viewMode === 'mine'
    ? `Top 20 interfaces I invoked · ${totalInvoke.toLocaleString()} total`
    : `Top 20 interfaces platform-wide · ${totalInvoke.toLocaleString()} total`;

  const option = {
    backgroundColor: 'transparent',
    title: {
      text: chartTitle,
      subtext: chartSubtext,
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
        color: ['#00D4AA', '#00A3FF', '#00BFA0', '#5CB85C', '#F0AD4E', '#5BC0DE', '#DDA0DD', '#FF6B6B', '#45B7D1', '#96CEB4', '#F0B429', '#A8E6CF'],
      },
    ],
  };

  return (
    <PageContainer>
      <div className="analysis-page">
        {/* View Toggle — only shown to admin */}
        {isAdmin && (
          <Card className="analysis-toggle-card" bodyStyle={{ padding: '16px 20px' }}>
            <div className="analysis-toggle-header">
              <AppstoreOutlined className="analysis-toggle-icon" />
              <span className="analysis-toggle-label">View Mode</span>
            </div>
            <Radio.Group
              value={viewMode}
              onChange={(e) => setViewMode(e.target.value)}
              buttonStyle="solid"
              className="analysis-radio-group"
            >
              <Radio.Button value="mine">
                <Space size={6}>
                  <UserOutlined />
                  <span>My Calls</span>
                </Space>
              </Radio.Button>
              <Radio.Button value="all">
                <Space size={6}>
                  <TeamOutlined />
                  <span>Platform Total</span>
                </Space>
              </Radio.Button>
            </Radio.Group>
          </Card>
        )}

        {/* Chart Card */}
        <Card className="analysis-card" bodyStyle={{ padding: '20px' }}>
          <div className="analysis-header">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" />
            </svg>
            <h3>Call Volume Distribution</h3>
            {isAdmin && (
              <span className="analysis-view-badge">
                {viewMode === 'mine' ? 'My Calls' : 'Platform Total'}
              </span>
            )}
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
        </Card>
      </div>
    </PageContainer>
  );
};

export default InterfaceAnalysis;
