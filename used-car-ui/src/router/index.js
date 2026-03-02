import { createRouter, createWebHistory } from 'vue-router'
import Login from '../views/Login.vue'
import Layout from '../views/Layout.vue'
import CarList from '../views/CarList.vue'
import MyOrders from '../views/MyOrders.vue'
import MyCars from '../views/MyCars.vue'
import CarDetail from '../views/CarDetail.vue'
import UserCenter from '../views/UserCenter.vue'
import ServiceCenter from '../views/ServiceCenter.vue'
import Community from '../views/Community.vue'
import Dashboard from '../views/admin/Dashboard.vue'

const routes = [
    { path: '/', redirect: '/login' },
    { path: '/login', component: Login },

    {
        path: '/',
        component: Layout,
        children: [
            { path: 'cars', component: CarList },
            { path: 'my-orders', component: MyOrders },
            { path: 'my-cars', component: MyCars },
            { path: 'car/:id', component: CarDetail },
            { path: 'user', component: UserCenter },
            
            { path: 'service', component: ServiceCenter },
            
            { path: 'community', component: Community },

            // 管理员路由
            { path: 'admin/dashboard', component: Dashboard },
        ]
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes
})

export default router