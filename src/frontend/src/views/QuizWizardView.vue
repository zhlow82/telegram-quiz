<template>
  <AppLayout>
    <!-- Header -->
    <div class="flex items-center gap-4 pb-6 mb-6 border-b border-slate-200 flex-wrap">
      <router-link
        to="/quizzes"
        class="w-9 h-9 rounded-lg border border-slate-200 bg-white hover:bg-slate-50 flex items-center justify-center text-slate-500 hover:text-slate-700 transition no-underline flex-shrink-0"
      >
        <ChevronLeft class="w-4 h-4" />
      </router-link>
      <div>
        <h1 class="text-2xl font-black text-slate-900 leading-tight">{{ isEditMode ? 'Edit Quiz' : 'Create Quiz' }}</h1>
        <p class="text-sm text-slate-500 mt-0.5">Step {{ step }} of {{ TOTAL_STEPS }}</p>
      </div>
    </div>

    <!-- Step progress bar -->
    <div class="flex items-center justify-between mb-16 max-w-3xl mx-auto">
      <div
        v-for="s in TOTAL_STEPS"
        :key="s"
        class="flex items-center"
        :class="s < TOTAL_STEPS ? 'flex-1' : ''"
      >
        <!-- Step circle -->
        <div class="relative flex items-center justify-center">
          <div
            class="w-10 h-10 rounded-full flex items-center justify-center font-bold text-sm transition-all duration-300 border-2"
            :class="{
              'bg-primary border-primary text-white': s <= step,
              'bg-white border-slate-300 text-slate-400': s > step
            }"
          >
            <CheckCircle v-if="s < step" class="w-5 h-5" />
            <span v-else>{{ s }}</span>
          </div>
          <!-- Step label -->
          <div class="absolute top-12 left-1/2 -translate-x-1/2 whitespace-nowrap">
            <span
              class="text-xs font-medium"
              :class="{
                'text-primary': s <= step,
                'text-slate-400': s > step
              }"
            >
              {{ getStepLabel(s) }}
            </span>
          </div>
        </div>
        <!-- Connector line -->
        <div
          v-if="s < TOTAL_STEPS"
          class="flex-1 h-0.5 mx-2 transition-colors duration-300"
          :class="s < step ? 'bg-primary' : 'bg-slate-200'"
        ></div>
      </div>
    </div>

    <!-- ── Step 1: BotFather instructions ─────────────────────────────── -->
    <transition name="step" mode="out-in">
    <div v-if="step === 1" key="step1" class="max-w-2xl space-y-5">
      <div class="bg-blue-50 border border-blue-200 rounded-2xl p-6">
        <div class="flex items-center gap-3 mb-4">
          <div class="w-10 h-10 rounded-xl bg-primary flex items-center justify-center flex-shrink-0">
            <Bot class="w-5 h-5 text-white" />
          </div>
          <div>
            <h2 class="text-base font-bold text-slate-900">Create a Telegram Bot</h2>
            <p class="text-sm text-slate-500">You'll need a bot token from BotFather</p>
          </div>
        </div>
        <ol class="space-y-3 text-sm text-slate-700">
          <li class="flex gap-3">
            <span class="w-6 h-6 rounded-full bg-primary text-white text-xs font-bold flex items-center justify-center flex-shrink-0 mt-0.5">1</span>
            <span>Open Telegram and search for <strong class="font-semibold">@BotFather</strong> (the official bot with a blue checkmark)</span>
          </li>
          <li class="flex gap-3">
            <span class="w-6 h-6 rounded-full bg-primary text-white text-xs font-bold flex items-center justify-center flex-shrink-0 mt-0.5">2</span>
            <span>Send the command <code class="bg-white border border-blue-200 px-1.5 py-0.5 rounded text-blue-700 font-mono text-xs">/newbot</code> to BotFather</span>
          </li>
          <li class="flex gap-3">
            <span class="w-6 h-6 rounded-full bg-primary text-white text-xs font-bold flex items-center justify-center flex-shrink-0 mt-0.5">3</span>
            <span>Follow the prompts — choose a name and a unique username ending in <code class="bg-white border border-blue-200 px-1.5 py-0.5 rounded text-blue-700 font-mono text-xs">bot</code></span>
          </li>
          <li class="flex gap-3">
            <span class="w-6 h-6 rounded-full bg-primary text-white text-xs font-bold flex items-center justify-center flex-shrink-0 mt-0.5">4</span>
            <span>BotFather will reply with your <strong class="font-semibold">bot token</strong> — a long string like <code class="bg-white border border-blue-200 px-1.5 py-0.5 rounded text-blue-700 font-mono text-xs">123456789:ABC...</code></span>
          </li>
          <li class="flex gap-3">
            <span class="w-6 h-6 rounded-full bg-primary text-white text-xs font-bold flex items-center justify-center flex-shrink-0 mt-0.5">5</span>
            <span>Copy that token — you'll paste it in the next step</span>
          </li>
        </ol>
      </div>

      <div class="bg-amber-50 border border-amber-200 rounded-xl px-4 py-3 flex items-start gap-2.5 text-sm text-amber-800">
        <AlertTriangle class="w-4 h-4 flex-shrink-0 mt-0.5" />
        <span>Keep your bot token secret — treat it like a password. Never share it publicly.</span>
      </div>
    </div>

    <!-- ── Step 2: Bot token input ─────────────────────────────────────── -->
    <div v-else-if="step === 2" key="step2" class="max-w-xl space-y-5">
      <div>
        <h2 class="text-lg font-bold text-slate-900 mb-1">Enter your bot token</h2>
        <p class="text-sm text-slate-500">Paste the token you received from BotFather</p>
      </div>

      <div class="space-y-2">
        <label class="block text-sm font-semibold text-slate-700">Bot Token</label>
        <div class="flex gap-2">
          <input
            v-model="botToken"
            type="password"
            placeholder="123456789:ABCDEFGHIJKLMNabcdefghijklmn"
            class="flex-1 px-3 py-2.5 text-sm border rounded-xl outline-none transition bg-white text-slate-900 placeholder-slate-400 font-mono"
            :class="tokenError ? 'border-red-400 focus:border-red-500 focus:ring-2 focus:ring-red-500/20' : 'border-slate-200 focus:border-primary focus:ring-2 focus:ring-primary/20'"
            @input="botValidated = false; tokenError = ''"
            @keydown.enter="validateToken"
          />
          <button
            class="px-4 py-2.5 rounded-full bg-primary hover:bg-primary-hover text-white text-sm font-semibold transition disabled:opacity-60 cursor-pointer disabled:cursor-not-allowed flex items-center gap-2"
            :disabled="!botToken.trim() || validating"
            @click="validateToken"
          >
            <span v-if="validating" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin inline-block"></span>
            {{ validating ? 'Checking…' : 'Validate' }}
          </button>
        </div>
        <p v-if="tokenError" class="text-xs text-red-600">{{ tokenError }}</p>
        <div v-if="botValidated" class="flex items-center gap-2 text-green-700 text-sm bg-green-50 border border-green-200 rounded-lg px-3 py-2">
          <CheckCircle class="w-4 h-4 flex-shrink-0" />
          Connected to <strong class="font-semibold">{{ botName }}</strong>
          <span class="text-green-500 text-xs">@{{ botUsername }}</span>
        </div>
      </div>
    </div>

    <!-- ── Step 3: Quiz config ─────────────────────────────────────────── -->
    <div v-else-if="step === 3" key="step3" class="max-w-xl space-y-5">
      <div>
        <h2 class="text-lg font-bold text-slate-900 mb-1">Quiz settings</h2>
        <p class="text-sm text-slate-500">Configure how your quiz will run</p>
      </div>

      <div class="space-y-4">
        <div class="space-y-1.5">
          <label class="block text-sm font-semibold text-slate-700">Quiz Name <span class="text-red-500">*</span></label>
          <input
            v-model="quizName"
            type="text"
            placeholder="e.g. Geography Round 1"
            class="w-full px-3 py-2.5 text-sm border border-slate-200 rounded-xl outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 bg-white text-slate-900 placeholder-slate-400 transition"
          />
        </div>

        <!-- Timing mode selector -->
        <div class="space-y-2">
          <label class="block text-sm font-semibold text-slate-700">Timing</label>
          <div class="grid grid-cols-3 gap-3">
            <button
              type="button"
              class="flex flex-col items-center gap-2 p-4 rounded-xl border-2 transition cursor-pointer"
              :class="timingMode === 'none'
                ? 'border-slate-500 bg-slate-50'
                : 'border-slate-200 bg-white hover:border-slate-300'"
              @click="timingMode = 'none'"
            >
              <TimerOff class="w-5 h-5" :class="timingMode === 'none' ? 'text-slate-600' : 'text-slate-400'" />
              <span class="text-xs font-semibold" :class="timingMode === 'none' ? 'text-slate-700' : 'text-slate-500'">No Timer</span>
            </button>
            <button
              type="button"
              class="flex flex-col items-center gap-2 p-4 rounded-xl border-2 transition cursor-pointer"
              :class="timingMode === 'per-question'
                ? 'border-primary bg-sky-50'
                : 'border-slate-200 bg-white hover:border-slate-300'"
              @click="timingMode = 'per-question'"
            >
              <Clock class="w-5 h-5" :class="timingMode === 'per-question' ? 'text-primary' : 'text-slate-400'" />
              <span class="text-xs font-semibold" :class="timingMode === 'per-question' ? 'text-primary' : 'text-slate-500'">Per Question</span>
            </button>
            <button
              type="button"
              class="flex flex-col items-center gap-2 p-4 rounded-xl border-2 transition cursor-pointer"
              :class="timingMode === 'total'
                ? 'border-violet-500 bg-violet-50'
                : 'border-slate-200 bg-white hover:border-slate-300'"
              @click="timingMode = 'total'"
            >
              <Timer class="w-5 h-5" :class="timingMode === 'total' ? 'text-violet-600' : 'text-slate-400'" />
              <span class="text-xs font-semibold" :class="timingMode === 'total' ? 'text-violet-700' : 'text-slate-500'">Total Quiz</span>
            </button>
          </div>
        </div>

        <!-- Per-question time input -->
        <div v-if="timingMode === 'per-question'" class="space-y-1.5">
          <label class="block text-sm font-semibold text-slate-700">Time per Question</label>
          <div class="relative max-w-48">
            <input
              v-model.number="timePerQuestion"
              type="number"
              min="5"
              max="300"
              class="w-full px-3 py-2.5 pr-10 text-sm border border-slate-200 rounded-xl outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 bg-white text-slate-900 transition"
            />
            <span class="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-slate-400">sec</span>
          </div>
          <p class="text-xs text-slate-400">5 – 300 seconds per question</p>
        </div>

        <!-- Total quiz time input -->
        <div v-if="timingMode === 'total'" class="space-y-1.5">
          <label class="block text-sm font-semibold text-slate-700">Total Quiz Time</label>
          <div class="relative max-w-48">
            <input
              v-model.number="totalQuizTimeSeconds"
              type="number"
              min="30"
              class="w-full px-3 py-2.5 pr-10 text-sm border border-slate-200 rounded-xl outline-none focus:border-violet-500 focus:ring-2 focus:ring-violet-500/20 bg-white text-slate-900 transition"
            />
            <span class="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-slate-400">sec</span>
          </div>
          <p class="text-xs text-slate-400">Minimum 30 seconds for the whole quiz</p>
        </div>

        <!-- Pass Score -->
        <div class="space-y-1.5">
          <label class="block text-sm font-semibold text-slate-700">Pass Score</label>
          <div class="relative max-w-48">
            <input
              v-model.number="passScorePercent"
              type="number"
              min="0"
              max="100"
              class="w-full px-3 py-2.5 pr-8 text-sm border border-slate-200 rounded-xl outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 bg-white text-slate-900 transition"
            />
            <span class="absolute right-3 top-1/2 -translate-y-1/2 text-xs text-slate-400">%</span>
          </div>
          <p class="text-xs text-slate-400">0 – 100%</p>
        </div>
      </div>
    </div>

    <!-- ── Step 4: Question selector ──────────────────────────────────── -->
    <div v-else-if="step === 4" key="step4" class="space-y-5">
      <div>
        <h2 class="text-lg font-bold text-slate-900 mb-1">Select questions</h2>
        <p class="text-sm text-slate-500">Choose questions from your bank. Drag to reorder selected ones.</p>
      </div>

      <div class="grid grid-cols-1 md:grid-cols-2 gap-4" style="min-height: 300px">
        <!-- Available questions -->
        <div class="space-y-2">
          <p class="text-xs font-semibold text-slate-500 uppercase tracking-wide">Available ({{ unselectedQuestions.length }})</p>
          <!-- Folder filter -->
          <div v-if="folders.length > 0" class="flex items-center gap-2">
            <FolderOpen class="w-4 h-4 text-slate-400 flex-shrink-0" />
            <select
              v-model="folderFilter"
              class="flex-1 text-sm text-slate-700 bg-white border border-slate-200 rounded-lg px-2.5 py-1.5 outline-none focus:border-primary focus:ring-2 focus:ring-primary/20 transition cursor-pointer"
            >
              <option :value="null">All folders</option>
              <option value="unfiled">Unfiled</option>
              <option v-for="f in folders" :key="f.id" :value="f.id">{{ f.name }}</option>
            </select>
          </div>
          <div class="flex items-center gap-2 px-3 py-2 bg-white border border-slate-200 rounded-lg">
            <Search class="w-4 h-4 text-slate-400 flex-shrink-0" />
            <input
              v-model="questionSearch"
              type="text"
              placeholder="Search questions…"
              class="flex-1 text-sm text-slate-700 placeholder-slate-400 bg-transparent outline-none"
            />
          </div>
          <!-- Question type filter tabs -->
          <div class="flex flex-wrap gap-1.5">
            <button
              type="button"
              class="px-2.5 py-1 rounded-lg text-xs font-semibold transition cursor-pointer border"
              :class="typeFilter === null ? 'bg-slate-700 text-white border-slate-700' : 'bg-white text-slate-600 border-slate-200 hover:bg-slate-50'"
              @click="typeFilter = null"
            >All</button>
            <button
              type="button"
              class="px-2.5 py-1 rounded-lg text-xs font-semibold transition cursor-pointer border"
              :class="typeFilter === 'mcq' ? 'bg-primary text-white border-primary' : 'bg-white text-primary border-primary/30 hover:bg-sky-50'"
              @click="typeFilter = 'mcq'"
            >MCQ</button>
            <button
              type="button"
              class="px-2.5 py-1 rounded-lg text-xs font-semibold transition cursor-pointer border"
              :class="typeFilter === 'photo' ? 'bg-violet-600 text-white border-violet-600' : 'bg-white text-violet-600 border-violet-200 hover:bg-violet-50'"
              @click="typeFilter = 'photo'"
            >Photo</button>
            <button
              type="button"
              class="px-2.5 py-1 rounded-lg text-xs font-semibold transition cursor-pointer border"
              :class="typeFilter === 'briefing' ? 'bg-amber-600 text-white border-amber-600' : 'bg-white text-amber-600 border-amber-200 hover:bg-amber-50'"
              @click="typeFilter = 'briefing'"
            >Briefing</button>
            <button
              type="button"
              class="px-2.5 py-1 rounded-lg text-xs font-semibold transition cursor-pointer border"
              :class="typeFilter === 'team' ? 'bg-emerald-600 text-white border-emerald-600' : 'bg-white text-emerald-600 border-emerald-200 hover:bg-emerald-50'"
              @click="typeFilter = 'team'"
            >Team Input</button>
          </div>
          <div class="bg-white border border-slate-200 rounded-xl overflow-hidden max-h-96 overflow-y-auto">
            <div v-if="questionsLoading" class="px-4 py-6 text-center text-sm text-slate-400">Loading…</div>
            <div v-else-if="allQuestions.length === 0" class="px-4 py-6 text-center text-sm text-slate-400 space-y-1">
              <p>No accessible questions found.</p>
              <p class="text-xs text-slate-400">This account can only use questions you created or questions shared with you.</p>
            </div>
            <div v-else-if="unselectedQuestions.length === 0" class="px-4 py-6 text-center text-sm text-slate-400">
              All questions selected
            </div>
            <button
              v-for="q in unselectedQuestions"
              :key="q.id"
              type="button"
              class="w-full text-left px-4 py-3 border-b border-slate-100 last:border-0 hover:bg-blue-50 transition-colors cursor-pointer flex items-center gap-3"
              @click="selectQuestion(q)"
            >
              <Plus class="w-4 h-4 text-primary flex-shrink-0" />
              <div v-if="q.questionBlocks.find(b => b.type === 'image')" class="relative w-10 h-10 flex-shrink-0">
                <img
                  :src="`/api/files/${q.questionBlocks.find(b => b.type === 'image')!.content}`"
                  class="w-10 h-10 rounded-lg object-cover border border-slate-200 bg-slate-100"
                  alt=""
                />
                <span
                  v-if="q.questionBlocks.filter(b => b.type === 'image').length > 1"
                  class="absolute -bottom-1 -right-1 bg-slate-700 text-white text-[0.55rem] font-bold leading-none px-1 py-0.5 rounded-md"
                >+{{ q.questionBlocks.filter(b => b.type === 'image').length - 1 }}</span>
              </div>
              <div v-else class="w-10 h-10 flex-shrink-0" />
              <div class="flex-1 min-w-0">
                <span class="block text-sm text-slate-800 leading-snug max-h-[2.5rem] overflow-hidden" style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">{{ stripHtml(q.questionBlocks.find(b => b.type === 'text')?.content || '') || '(no text)' }}</span>
                <div class="flex gap-1.5 mt-1 flex-wrap items-center">
                  <span class="text-[0.65rem] font-semibold px-1.5 py-0.5 rounded-full leading-none" :class="questionTypeBadgeClass(q)">{{ questionTypeLabel(q) }}</span>
                </div>
              </div>
              <div v-if="!q.isBriefing && q.mark != null && q.mark > 0" class="flex flex-col items-center justify-center w-9 flex-shrink-0">
                <span class="text-sm font-bold text-slate-700 leading-none">{{ q.mark }}</span>
                <span class="text-[0.6rem] font-medium text-slate-400 uppercase tracking-wide leading-none mt-0.5">pts</span>
              </div>
              <div v-else class="w-9 flex-shrink-0" />
            </button>
          </div>
        </div>

        <!-- Selected questions (draggable) -->
        <div class="space-y-2">
          <p class="text-xs font-semibold text-slate-500 uppercase tracking-wide">Selected ({{ selectedQuestions.length }})</p>
          <!-- Points summary card -->
          <div v-if="selectedQuestions.length > 0" class="flex items-stretch gap-2 bg-white border border-slate-200 rounded-xl overflow-hidden">
            <div class="flex-1 flex flex-col items-center justify-center py-3 px-4 border-r border-slate-100">
              <span class="text-2xl font-black text-slate-900 leading-none">{{ totalPoints }}</span>
              <span class="text-[0.65rem] font-semibold text-slate-400 uppercase tracking-wide mt-1">Total pts</span>
            </div>
            <div class="flex-1 flex flex-col items-center justify-center py-3 px-4">
              <span class="text-2xl font-black leading-none" :class="passScorePercent > 0 ? 'text-emerald-600' : 'text-slate-300'">{{ passingPoints }}</span>
              <span class="text-[0.65rem] font-semibold text-slate-400 uppercase tracking-wide mt-1">Pass at ({{ passScorePercent }}%)</span>
            </div>
          </div>
          <div class="bg-white border border-slate-200 rounded-xl overflow-hidden max-h-96 overflow-y-auto">
            <div v-if="selectedQuestions.length === 0" class="px-4 py-6 text-center text-sm text-slate-400">
              Click questions to add them
            </div>
            <VueDraggable
              v-else
              v-model="selectedQuestions"
              handle=".q-drag-handle"
              :animation="150"
            >
              <div
                v-for="(q, i) in selectedQuestions"
                :key="q.id"
                class="flex items-center gap-3 px-3 py-3 border-b border-slate-100 last:border-0 hover:bg-slate-50 transition-colors group"
              >
                <button type="button" class="q-drag-handle cursor-grab text-slate-300 hover:text-slate-400 active:cursor-grabbing flex-shrink-0">
                  <GripVertical class="w-4 h-4" />
                </button>
                <span class="text-xs font-bold text-slate-300 w-5 flex-shrink-0 text-right">{{ i + 1 }}</span>
                <div v-if="q.questionBlocks.find(b => b.type === 'image')" class="relative w-10 h-10 flex-shrink-0">
                  <img
                    :src="`/api/files/${q.questionBlocks.find(b => b.type === 'image')!.content}`"
                    class="w-10 h-10 rounded-lg object-cover border border-slate-200 bg-slate-100"
                    alt=""
                  />
                  <span
                    v-if="q.questionBlocks.filter(b => b.type === 'image').length > 1"
                    class="absolute -bottom-1 -right-1 bg-slate-700 text-white text-[0.55rem] font-bold leading-none px-1 py-0.5 rounded-md"
                  >+{{ q.questionBlocks.filter(b => b.type === 'image').length - 1 }}</span>
                </div>
                <div v-else class="w-10 h-10 flex-shrink-0" />
                <div class="flex-1 min-w-0">
                  <span class="block text-sm text-slate-800 leading-snug max-h-[2.5rem] overflow-hidden" style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">{{ stripHtml(q.questionBlocks.find(b => b.type === 'text')?.content || '') || '(no text)' }}</span>
                  <div class="flex gap-1.5 mt-1 flex-wrap items-center">
                    <span class="text-[0.65rem] font-semibold px-1.5 py-0.5 rounded-full leading-none" :class="questionTypeBadgeClass(q)">{{ questionTypeLabel(q) }}</span>
                  </div>
                </div>
                <div v-if="!q.isBriefing && q.mark != null && q.mark > 0" class="flex flex-col items-center justify-center w-9 flex-shrink-0">
                  <span class="text-sm font-bold text-slate-700 leading-none">{{ q.mark }}</span>
                  <span class="text-[0.6rem] font-medium text-slate-400 uppercase tracking-wide leading-none mt-0.5">pts</span>
                </div>
                <div v-else class="w-9 flex-shrink-0" />
                <button type="button" class="w-6 h-6 flex items-center justify-center rounded-md text-slate-300 hover:text-red-500 hover:bg-red-50 transition flex-shrink-0 cursor-pointer" @click="deselectQuestion(q)">
                  <X class="w-3.5 h-3.5" />
                </button>
              </div>
            </VueDraggable>
          </div>
        </div>
      </div>
    </div>

    <!-- ── Step 5: Review ──────────────────────────────────────────────── -->
    <div v-else-if="step === 5" key="step5" class="max-w-2xl space-y-5">
      <div>
        <h2 class="text-lg font-bold text-slate-900 mb-1">Review & Create</h2>
        <p class="text-sm text-slate-500">Everything look good? Hit Create to launch your quiz.</p>
      </div>

      <!-- Config summary -->
      <div class="bg-white border border-slate-200 rounded-2xl divide-y divide-slate-100">
        <div class="px-5 py-4 flex items-center justify-between gap-4">
          <span class="text-sm text-slate-500 flex items-center gap-2"><Bot class="w-4 h-4" />Bot</span>
          <span class="text-sm font-semibold text-slate-900">{{ botName }} <span class="font-normal text-slate-500">@{{ botUsername }}</span></span>
        </div>
        <div class="px-5 py-4 flex items-center justify-between gap-4">
          <span class="text-sm text-slate-500 flex items-center gap-2"><Zap class="w-4 h-4" />Quiz Name</span>
          <span class="text-sm font-semibold text-slate-900">{{ quizName }}</span>
        </div>
        <div class="px-5 py-4 flex items-center justify-between gap-4">
          <span class="text-sm text-slate-500 flex items-center gap-2">
            <TimerOff v-if="timingMode === 'none'" class="w-4 h-4" />
            <Clock v-else-if="timingMode === 'per-question'" class="w-4 h-4" />
            <Timer v-else class="w-4 h-4" />
            Timing
          </span>
          <span class="text-sm font-semibold text-slate-900">
            <template v-if="timingMode === 'none'">No timer</template>
            <template v-else-if="timingMode === 'per-question'">{{ timePerQuestion }}s per question</template>
            <template v-else>{{ totalQuizTimeDisplay }} total</template>
          </span>
        </div>
        <div class="px-5 py-4 flex items-center justify-between gap-4">
          <span class="text-sm text-slate-500 flex items-center gap-2"><CheckCircle class="w-4 h-4" />Pass Score</span>
          <span class="text-sm font-semibold text-slate-900">{{ passScorePercent }}%</span>
        </div>
        <div v-if="totalPoints > 0" class="px-5 py-4 flex items-center justify-between gap-4">
          <span class="text-sm text-slate-500 flex items-center gap-2"><Trophy class="w-4 h-4" />Points</span>
          <span class="text-sm font-semibold text-slate-900">
            {{ totalPoints }} total
            <span v-if="passScorePercent > 0" class="font-normal text-slate-500">· pass at <span class="text-emerald-600 font-semibold">{{ passingPoints }}</span></span>
          </span>
        </div>
      </div>

      <!-- Questions list -->
      <div class="space-y-1.5">
        <div class="flex items-center justify-between">
          <p class="text-xs font-semibold text-slate-500 uppercase tracking-wide">Questions ({{ selectedQuestions.length }})</p>
        </div>
        <div class="bg-white border border-slate-200 rounded-2xl divide-y divide-slate-100">
          <div
            v-for="(q, i) in selectedQuestions"
            :key="q.id"
            class="flex items-center gap-3 px-4 py-3"
          >
            <span class="text-xs font-bold text-slate-300 w-5 flex-shrink-0 text-right">{{ i + 1 }}</span>
            <div v-if="q.questionBlocks.find(b => b.type === 'image')" class="relative w-8 h-8 flex-shrink-0">
              <img
                :src="`/api/files/${q.questionBlocks.find(b => b.type === 'image')!.content}`"
                class="w-8 h-8 rounded-lg object-cover border border-slate-200 bg-slate-100"
                alt=""
              />
              <span
                v-if="q.questionBlocks.filter(b => b.type === 'image').length > 1"
                class="absolute -bottom-1 -right-1 bg-slate-700 text-white text-[0.55rem] font-bold leading-none px-1 py-0.5 rounded-md"
              >+{{ q.questionBlocks.filter(b => b.type === 'image').length - 1 }}</span>
            </div>
            <div v-else class="w-8 h-8 flex-shrink-0" />
            <div class="flex-1 min-w-0">
              <span class="block text-sm text-slate-800 leading-snug max-h-[2.5rem] overflow-hidden" style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;">{{ stripHtml(q.questionBlocks.find(b => b.type === 'text')?.content || '') || '(no text)' }}</span>
              <div class="flex gap-1.5 mt-1 flex-wrap items-center">
                <span class="text-[0.65rem] font-semibold px-1.5 py-0.5 rounded-full leading-none" :class="questionTypeBadgeClass(q)">{{ questionTypeLabel(q) }}</span>
              </div>
            </div>
            <div v-if="!q.isBriefing && q.mark != null && q.mark > 0" class="flex flex-col items-center justify-center w-9 flex-shrink-0">
              <span class="text-sm font-bold text-slate-700 leading-none">{{ q.mark }}</span>
              <span class="text-[0.6rem] font-medium text-slate-400 uppercase tracking-wide leading-none mt-0.5">pts</span>
            </div>
            <div v-else class="w-9 flex-shrink-0" />
          </div>
        </div>
      </div>

      <p v-if="submitError" class="text-sm text-red-600">{{ submitError }}</p>
    </div>
    </transition>

    <!-- Navigation -->
    <div class="flex items-center justify-between gap-3 mt-8 pt-6 border-t border-slate-200">
      <button
        v-if="step > 1"
        type="button"
        class="px-4 py-2.5 rounded-lg border border-slate-200 text-sm font-medium text-slate-600 hover:bg-slate-50 transition cursor-pointer"
        @click="step--"
      >
        ← Back
      </button>
      <div v-else />
      <div class="flex items-center gap-3">
        <button
          type="button"
          class="px-4 py-2.5 rounded-lg border border-slate-200 text-sm font-medium text-slate-600 hover:bg-slate-50 transition cursor-pointer"
          @click="router.push('/quizzes')"
        >
          Cancel
        </button>
        <button
          v-if="step < TOTAL_STEPS"
          type="button"
          class="px-5 py-2.5 rounded-full bg-primary hover:bg-primary-hover text-white text-sm font-semibold transition cursor-pointer disabled:opacity-60 disabled:cursor-not-allowed"
          :disabled="!canProceed"
          @click="nextStep"
        >
          Continue →
        </button>
        <button
          v-if="step === TOTAL_STEPS"
          type="button"
          class="px-5 py-2.5 rounded-full bg-primary hover:bg-primary-hover text-white text-sm font-semibold transition cursor-pointer disabled:opacity-60 disabled:cursor-not-allowed inline-flex items-center gap-2"
          :disabled="submitting"
          @click="isEditMode ? updateQuiz() : createQuiz()"
        >
          <span v-if="submitting" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin inline-block"></span>
          {{ submitting ? (isEditMode ? 'Saving…' : 'Creating…') : (isEditMode ? 'Save Changes' : 'Create Quiz') }}
        </button>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { VueDraggable } from 'vue-draggable-plus'
import {
  ChevronLeft, Plus, X, GripVertical, Bot, Zap, Clock,
  CheckCircle, AlertTriangle, Search, TimerOff, Timer, FolderOpen, Trophy
} from '@lucide/vue'
import AppLayout from '@/components/AppLayout.vue'
import { quizService } from '@/services/quizService'
import { questionsService } from '@/services/questionsService'
import { foldersService } from '@/services/foldersService'
import { useToast } from '@/composables/useToast'
import type { Question } from '@/types/question'
import type { Folder } from '@/types/folder'

function stripHtml(html: string): string {
  return html.replace(/<[^>]*>/g, '').trim()
}

function questionTypeLabel(q: Question): string {
  if (q.isBriefing) return 'Briefing'
  if (q.expectPhoto) return 'Photo'
  if (q.expectsTextInput) return 'Team Input'
  if (q.options && q.options.length > 0) return 'Multiple Choice'
  return 'Open'
}

function questionTypeBadgeClass(q: Question): string {
  if (q.isBriefing) return 'text-slate-600 bg-slate-100'
  if (q.expectPhoto) return 'text-emerald-700 bg-emerald-50'
  if (q.expectsTextInput) return 'text-violet-700 bg-violet-50'
  if (q.options && q.options.length > 0) return 'text-blue-700 bg-blue-50'
  return 'text-slate-600 bg-slate-100'
}

function getStepLabel(step: number): string {
  const labels = ['Bot Setup', 'Token', 'Settings', 'Questions', 'Review']
  return labels[step - 1] || ''
}

const router = useRouter()
const route = useRoute()
const toast = useToast()

const isEditMode = computed(() => !!route.params.id)
const editQuizId = computed(() => route.params.id ? Number(route.params.id) : null)

const TOTAL_STEPS = 5
const step = ref(1)

// Step 2 — bot token
const botToken = ref('')
const botValidated = ref(false)
const botName = ref('')
const botUsername = ref('')
const validating = ref(false)
const tokenError = ref('')

// Step 3 — quiz config
const quizName = ref('')
type TimingMode = 'none' | 'per-question' | 'total'
const timingMode = ref<TimingMode>('per-question')
const timePerQuestion = ref(30)
const totalQuizTimeSeconds = ref(600)
const passScorePercent = ref(60)

const totalQuizTimeDisplay = computed(() => {
  const s = totalQuizTimeSeconds.value
  if (s >= 60) {
    const m = Math.floor(s / 60)
    const rem = s % 60
    return rem > 0 ? `${m}m ${rem}s` : `${m}m`
  }
  return `${s}s`
})

const totalPoints = computed(() =>
  selectedQuestions.value.reduce((sum, q) => sum + (q.mark ?? 0), 0)
)

const passingPoints = computed(() =>
  Math.ceil(totalPoints.value * passScorePercent.value / 100)
)

// Step 4 — questions
const allQuestions = ref<Question[]>([])
const selectedQuestions = ref<Question[]>([])
const questionsLoading = ref(false)
const questionSearch = ref('')
const folders = ref<Folder[]>([])
const folderFilter = ref<number | 'unfiled' | null>(null)
const typeFilter = ref<'mcq' | 'photo' | 'briefing' | 'team' | null>(null)

// Step 5
const submitting = ref(false)
const submitError = ref('')

const hasUnsavedChanges = computed(() => {
  return botToken.value.trim().length > 0
    || quizName.value.trim().length > 0
    || selectedQuestions.value.length > 0
    || step.value > 1
})

function onBeforeUnload(e: BeforeUnloadEvent) {
  if (hasUnsavedChanges.value) {
    e.preventDefault()
  }
}

onMounted(() => {
  window.addEventListener('beforeunload', onBeforeUnload)
  loadQuestions()
  loadFolders()
  if (isEditMode.value && editQuizId.value) {
    loadQuizForEdit()
  }
})

async function loadQuizForEdit() {
  if (!editQuizId.value) return
  try {
    const quiz = await quizService.get(editQuizId.value)
    quizName.value = quiz.name
    passScorePercent.value = quiz.passScorePercent
    if (quiz.totalTimeLimitSeconds > 0) {
      timingMode.value = 'total'
      totalQuizTimeSeconds.value = quiz.totalTimeLimitSeconds
    } else if (quiz.timePerQuestionSeconds > 0) {
      timingMode.value = 'per-question'
      timePerQuestion.value = quiz.timePerQuestionSeconds
    } else {
      timingMode.value = 'none'
    }
    botName.value = quiz.botUsername || ''
    botUsername.value = quiz.botUsername || ''
    botToken.value = quiz.botTokenMasked || ''
    botValidated.value = true
    if (quiz.questions) {
      selectedQuestions.value = quiz.questions
    }
    step.value = 4
  } catch {
    toast.error('Failed to load quiz for editing')
    router.push('/quizzes')
  }
}

onBeforeUnmount(() => {
  window.removeEventListener('beforeunload', onBeforeUnload)
})

const unselectedQuestions = computed(() => {
  const selectedIds = new Set(selectedQuestions.value.map(q => q.id))
  const search = questionSearch.value.trim().toLowerCase()
  return allQuestions.value.filter(q => {
    if (selectedIds.has(q.id)) return false
    if (folderFilter.value === 'unfiled' && q.folderId !== null) return false
    if (typeof folderFilter.value === 'number' && q.folderId !== folderFilter.value) return false
    if (typeFilter.value === 'mcq' && (!q.options || q.options.length === 0)) return false
    if (typeFilter.value === 'photo' && !q.expectPhoto) return false
    if (typeFilter.value === 'briefing' && !q.isBriefing) return false
    if (typeFilter.value === 'team' && !q.expectsTextInput) return false
    if (!search) return true
    const text = q.questionBlocks.find(b => b.type === 'text')?.content || ''
    return text.toLowerCase().includes(search)
  })
})

const isQuizConfigValid = computed(() => {
  const hasName = quizName.value.trim().length > 0
  const hasValidPassScore = Number.isFinite(passScorePercent.value)
    && passScorePercent.value >= 0
    && passScorePercent.value <= 100

  let hasValidTiming = true
  if (timingMode.value === 'per-question') {
    hasValidTiming = Number.isFinite(timePerQuestion.value)
      && timePerQuestion.value >= 5
      && timePerQuestion.value <= 300
  } else if (timingMode.value === 'total') {
    hasValidTiming = Number.isFinite(totalQuizTimeSeconds.value)
      && totalQuizTimeSeconds.value >= 30
  }

  return hasName && hasValidTiming && hasValidPassScore
})

const canProceed = computed(() => {
  if (step.value === 1) return true
  if (step.value === 2) return botValidated.value
  if (step.value === 3) return isQuizConfigValid.value
  if (step.value === 4) return selectedQuestions.value.length > 0
  return true
})

async function loadQuestions() {
  questionsLoading.value = true
  try {
    allQuestions.value = await questionsService.list()
  } finally {
    questionsLoading.value = false
  }
}

async function loadFolders() {
  try {
    folders.value = await foldersService.list()
  } catch {
    // folders are optional for filtering — ignore errors
  }
}

async function validateToken() {
  if (!botToken.value.trim() || validating.value) return
  validating.value = true
  tokenError.value = ''
  botValidated.value = false
  try {
    const res = await quizService.validateToken({ token: botToken.value.trim() })
    if (res.valid) {
      botValidated.value = true
      botName.value = res.botName ?? ''
      botUsername.value = res.username ?? ''
    } else {
      tokenError.value = 'Invalid token. Please check and try again.'
    }
  } catch {
    tokenError.value = 'Could not validate token. Check your connection.'
  } finally {
    validating.value = false
  }
}

function selectQuestion(q: Question) {
  if (!selectedQuestions.value.find(s => s.id === q.id)) {
    selectedQuestions.value.push(q)
  }
}

function deselectQuestion(q: Question) {
  selectedQuestions.value = selectedQuestions.value.filter(s => s.id !== q.id)
}

function nextStep() {
  if (!canProceed.value) return
  step.value++
}

async function createQuiz() {
  submitting.value = true
  submitError.value = ''
  try {
    await quizService.create({
      name: quizName.value.trim(),
      botToken: botToken.value.trim(),
      botUsername: botUsername.value.trim() || undefined,
      timePerQuestionSeconds: timingMode.value === 'per-question' ? timePerQuestion.value : 0,
      totalTimeLimitSeconds: timingMode.value === 'total' ? totalQuizTimeSeconds.value : 0,
      passScorePercent: passScorePercent.value,
      questionIds: selectedQuestions.value.map(q => q.id),
    })
    toast.success('Quiz created successfully')
    router.push('/quizzes')
  } catch {
    submitError.value = 'Failed to create quiz. Please try again.'
  } finally {
    submitting.value = false
  }
}

async function updateQuiz() {
  if (!editQuizId.value) return
  submitting.value = true
  submitError.value = ''
  try {
    await quizService.update(editQuizId.value, {
      name: quizName.value.trim(),
      botToken: botToken.value.trim() || undefined,
      botUsername: botUsername.value.trim() || undefined,
      timePerQuestionSeconds: timingMode.value === 'per-question' ? timePerQuestion.value : 0,
      totalTimeLimitSeconds: timingMode.value === 'total' ? totalQuizTimeSeconds.value : 0,
      passScorePercent: passScorePercent.value,
      questionIds: selectedQuestions.value.map(q => q.id),
    })
    toast.success('Quiz updated successfully')
    router.push('/quizzes')
  } catch {
    submitError.value = 'Failed to update quiz. Please try again.'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.step-enter-active,
.step-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.step-enter-from {
  opacity: 0;
  transform: translateX(20px);
}
.step-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>
