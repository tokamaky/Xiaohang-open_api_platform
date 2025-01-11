import { PageContainer } from '@ant-design/pro-components';
import React, { useEffect, useState } from 'react';
import ReactECharts from 'echarts-for-react';
import { listTopInvokeInterfaceInfoUsingGet } from "@/services/xiaohang-backend/analysisController";

const InterfaceAnalysis: React.FC = () => {
  const [data, setData] = useState<API.InterfaceInfoVO[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    try {
      listTopInvokeInterfaceInfoUsingGet().then((res) => {
        console.log("API Response:", res); // Debugging
        if (res.data) {
          setData(res.data);
        }
        setLoading(false);
      });
    } catch (e: any) {
      console.log(e);
    }
  }, []);

  const chartData = data
    .filter((item) => item.totalNum !== undefined) // Ensure totalNum is defined
    .sort((a, b) => (b.totalNum ?? 0) - (a.totalNum ?? 0)) // Provide a default value for undefined totalNum
    .slice(0, 3) // Take the top 3
    .map((item) => {
      return {
        name: item.name,
        value: item.totalNum ?? 0, // Provide a default value for undefined totalNum
      };
    });


  const option = {
    title: {
      text: 'Invocation Count Statistics',
      subtext: 'TOP 3 APIs',
      left: 'center',
    },
    tooltip: {
      trigger: 'item',
    },
    legend: {
      orient: 'vertical',
      left: 'left',
    },
    series: [
      {
        name: 'Invocation Count',
        type: 'pie',
        radius: '50%',
        data: chartData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
      },
    ],
  };

  return (
    <PageContainer>
      <ReactECharts showLoading={loading} option={option} />
    </PageContainer>
  );
};

export default InterfaceAnalysis;
