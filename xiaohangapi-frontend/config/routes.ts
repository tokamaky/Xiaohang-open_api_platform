export default [
  { path: '/', name: 'Homepage', icon: 'smile', component: './Index' },
  { path: '/interface_info/:id', name: 'View Interface', icon: 'smile', component: './InterfaceInfo', hideInMenu: true },
  {
    path: '/user',
    layout: false,
    routes: [{ name: '登录', path: '/user/login', component: './User/Login' }],
  },
  {
    path: '/admin',
    name: 'Admin Page',
    icon: 'crown',
    access: 'canAdmin',
    routes: [
      { name: 'Interface Management', icon: 'table', path: '/admin/interface_info', component: './Admin/InterfaceInfo' },
      { name: 'Interface Analysis', icon: 'analysis', path: '/admin/interface_analysis', component: './Admin/InterfaceAnalysis' },
    ],
  },

  // { path: '/', redirect: '/welcome' },
  { name: '个人中心', icon: 'UserOutlined', path: '/profile', component: './User/Profile' },
  { path: '*', layout: false, component: './404' },
];

