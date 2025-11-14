import './assets/main.css'
// Import Swiper styles
import 'swiper/css'
import 'swiper/css/navigation'
import 'swiper/css/pagination'
import 'jsvectormap/dist/jsvectormap.css'
import 'flatpickr/dist/flatpickr.css'

import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import VueApexCharts from 'vue3-apexcharts'
import { vMaska } from 'maska/vue';

const app = createApp(App)
app.directive('maska', vMaska)

app.use(router)
app.use(VueApexCharts)

app.mount('#app')
