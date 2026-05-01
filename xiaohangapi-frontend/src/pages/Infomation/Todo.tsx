import React, { useEffect } from 'react';
import { useNavigate } from '@umijs/max';

const Todo: React.FC = () => {
  const navigate = useNavigate();
  useEffect(() => {
    navigate('/', { replace: true });
  }, [navigate]);
  return null;
};

export default Todo;
