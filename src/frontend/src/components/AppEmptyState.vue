<template>
  <div class="flex flex-col items-center justify-center py-16 px-6 text-center">
    <div class="relative mb-5">
      <div
        class="w-20 h-20 rounded-2xl flex items-center justify-center"
        :class="iconBgClass"
      >
        <component :is="iconComponent" class="w-10 h-10" :class="iconClass" />
      </div>
      <div
        v-if="showAddButton"
        class="absolute -top-2 -right-2 w-8 h-8 rounded-full flex items-center justify-center shadow-sm"
        :class="addButtonClass"
      >
        <Plus class="w-5 h-5 text-white" />
      </div>
    </div>
    <h3 class="font-bold text-slate-900 text-lg mb-2">{{ title }}</h3>
    <p class="text-sm text-slate-500 max-w-sm mb-5">{{ description }}</p>
    <slot>
      <button
        v-if="actionLabel"
        class="inline-flex items-center gap-2 bg-primary hover:bg-primary-hover text-white text-sm font-semibold px-5 py-2.5 rounded-full transition cursor-pointer shadow-sm"
        @click="$emit('action')"
      >
        <Plus v-if="actionIcon === 'plus'" class="w-4 h-4" />
        {{ actionLabel }}
      </button>
    </slot>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Plus, BookOpen, Zap, Users, FolderOpen, Search, FileText, KeyRound } from '@lucide/vue'
import type { Component } from 'vue'

const props = withDefaults(defineProps<{
  icon?: string
  title: string
  description: string
  actionLabel?: string
  actionIcon?: 'plus' | 'none'
  showAddButton?: boolean
  variant?: 'blue' | 'green' | 'amber' | 'slate' | 'purple'
}>(), {
  actionIcon: 'plus',
  showAddButton: false,
  variant: 'blue'
})

defineEmits<{
  (e: 'action'): void
}>()

const iconMap: Record<string, Component> = {
  'book': BookOpen,
  'zap': Zap,
  'users': Users,
  'folder': FolderOpen,
  'search': Search,
  'file': FileText,
  'key': KeyRound,
}

const iconComponent = computed(() => (props.icon ? iconMap[props.icon] : null) || BookOpen)

const variantClasses: Record<string, { bg: string; icon: string; add: string }> = {
  blue: { bg: 'bg-gradient-to-br from-blue-100 to-blue-50', icon: 'text-blue-600', add: 'bg-blue-500' },
  green: { bg: 'bg-gradient-to-br from-green-100 to-green-50', icon: 'text-green-600', add: 'bg-green-500' },
  amber: { bg: 'bg-gradient-to-br from-amber-100 to-amber-50', icon: 'text-amber-600', add: 'bg-amber-500' },
  slate: { bg: 'bg-gradient-to-br from-slate-100 to-slate-50', icon: 'text-slate-600', add: 'bg-slate-500' },
  purple: { bg: 'bg-gradient-to-br from-purple-100 to-purple-50', icon: 'text-purple-600', add: 'bg-purple-500' },
}

const defaultVariant = variantClasses['blue']

const iconBgClass = computed(() => variantClasses[props.variant]?.bg ?? defaultVariant.bg)
const iconClass = computed(() => variantClasses[props.variant]?.icon ?? defaultVariant.icon)
const addButtonClass = computed(() => variantClasses[props.variant]?.add ?? defaultVariant.add)
</script>
