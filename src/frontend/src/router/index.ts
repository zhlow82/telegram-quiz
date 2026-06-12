import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import LoginView from '@/views/LoginView.vue'
import HomeView from '@/views/HomeView.vue'
import QuestionBankView from '@/views/QuestionBankView.vue'
import QuizListView from '@/views/QuizListView.vue'
import QuizParticipantsView from '@/views/QuizParticipantsView.vue'
import QuizWizardView from '@/views/QuizWizardView.vue'
import AdminUsersView from '@/views/AdminUsersView.vue'
import AdminInvitationCodesView from '@/views/AdminInvitationCodesView.vue'
import AdminSettingsView from '@/views/AdminSettingsView.vue'
import AdminLogsView from '@/views/AdminLogsView.vue'
import OAuth2CallbackView from '@/views/OAuth2CallbackView.vue'
import OAuth2RegisterView from '@/views/OAuth2RegisterView.vue'
import NotFoundView from '@/views/NotFoundView.vue'

const router = createRouter({
  history: createWebHistory('/tg-quiz/'),
  routes: [
    {
      path: '/',
      redirect: '/home'
    },
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { requiresGuest: true }
    },
    {
      path: '/home',
      name: 'home',
      component: HomeView,
      meta: { requiresAuth: true }
    },
    {
      path: '/questions',
      name: 'questions',
      component: QuestionBankView,
      meta: { requiresAuth: true }
    },
    {
      path: '/quizzes',
      name: 'quizzes',
      component: QuizListView,
      meta: { requiresAuth: true }
    },
    {
      path: '/quizzes/new',
      name: 'quiz-new',
      component: QuizWizardView,
      meta: { requiresAuth: true }
    },
    {
      path: '/quizzes/:id/edit',
      name: 'quiz-edit',
      component: QuizWizardView,
      meta: { requiresAuth: true }
    },
    {
      path: '/quizzes/:id/participants',
      name: 'quiz-participants',
      component: QuizParticipantsView,
      meta: { requiresAuth: true }
    },
    {
      path: '/admin/users',
      name: 'admin-users',
      component: AdminUsersView,
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/invitation-codes',
      name: 'admin-codes',
      component: AdminInvitationCodesView,
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/settings',
      name: 'admin-settings',
      component: AdminSettingsView,
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/admin/logs',
      name: 'admin-logs',
      component: AdminLogsView,
      meta: { requiresAuth: true, requiresAdmin: true }
    },
    {
      path: '/oauth2/callback',
      name: 'oauth2-callback',
      component: OAuth2CallbackView
    },
    {
      path: '/oauth2/register',
      name: 'oauth2-register',
      component: OAuth2RegisterView
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: NotFoundView
    }
  ]
})

router.beforeEach((to) => {
  const authStore = useAuthStore()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return '/login'
  }

  if (to.meta.requiresGuest && authStore.isAuthenticated) {
    return '/home'
  }

  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return '/home'
  }
})

export default router
