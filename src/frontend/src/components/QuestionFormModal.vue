<template>
  <teleport to="body">
    <transition
      enter-active-class="transition-opacity duration-150"
      enter-from-class="opacity-0"
      enter-to-class="opacity-100"
      leave-active-class="transition-opacity duration-150"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div v-if="visible" class="fixed inset-0 z-50 flex items-center justify-center p-4">
        <!-- Backdrop -->
        <div class="absolute inset-0 bg-black/50" @click="$emit('close')" />

        <!-- Modal panel -->
        <div
          class="relative bg-white rounded-2xl shadow-2xl flex flex-col w-full overflow-hidden"
          style="width: 90vw; max-width: 90vw; height: 90vh; max-height: 90vh"
          role="dialog"
          aria-modal="true"
        >

          <!-- Header -->
          <div class="flex items-center justify-between px-6 py-4 border-b border-slate-100">
            <div class="flex items-center gap-3">
              <div class="w-9 h-9 rounded-xl bg-blue-600 flex items-center justify-center text-white font-bold text-base shrink-0">
                {{ isEdit ? '✎' : '+' }}
              </div>
              <h2 class="text-lg font-bold text-slate-900 leading-tight">{{ isEdit ? 'Edit Question' : 'New Question' }}</h2>
            </div>
            <button
              type="button"
              class="w-8 h-8 rounded-lg flex items-center justify-center text-slate-400 hover:text-slate-600 hover:bg-slate-100 transition cursor-pointer"
              aria-label="Close"
              @click="$emit('close')"
            >✕</button>
          </div>

          <!-- Tabs -->
          <div class="flex gap-1 border-b border-slate-200 px-4 pt-1">
            <button
              v-for="tab in tabs"
              :key="tab.value"
              type="button"
              class="px-4 py-2 text-sm font-semibold rounded-t-lg border border-b-0 -mb-px transition cursor-pointer"
              :class="activeTab === tab.value
                ? 'bg-white border-slate-200 text-blue-600 shadow-sm'
                : 'bg-slate-50 border-transparent text-slate-500 hover:text-slate-700 hover:bg-slate-100'"
              @click="activeTab = tab.value"
            >{{ tab.label }}</button>
          </div>

          <!-- Body -->
          <div class="flex flex-1 overflow-hidden min-h-0">
            <!-- Form panel -->
            <div class="w-1/2 min-w-0 overflow-y-auto border-r border-slate-100">
              <form @submit.prevent="submit" class="p-6 space-y-5">

                <!-- ── Tab: Question ── -->
                <template v-if="activeTab === 'question'">

                  <!-- Question Type -->
                  <div class="space-y-2">
                    <label class="block text-sm font-semibold text-slate-700">Question Type</label>
                    <div class="grid grid-cols-3 gap-3">
                      <button
                        type="button"
                        class="flex flex-col items-center gap-2 px-3 py-4 rounded-xl border-2 transition cursor-pointer text-center"
                        :class="!form.expectPhoto && !form.isBriefing
                          ? 'border-blue-500 bg-blue-50 shadow-sm'
                          : 'border-slate-200 bg-slate-50 hover:border-slate-300 hover:bg-white'"
                        @click="form.expectPhoto = false; form.isBriefing = false"
                      >
                        <span class="text-2xl leading-none">⌨️</span>
                        <span class="text-sm font-bold" :class="!form.expectPhoto && !form.isBriefing ? 'text-blue-700' : 'text-slate-800'">Quick Reply</span>
                        <span class="text-xs text-slate-500 leading-snug">Players pick from A / B / C / D options</span>
                      </button>
                      <button
                        type="button"
                        class="flex flex-col items-center gap-2 px-3 py-4 rounded-xl border-2 transition cursor-pointer text-center"
                        :class="form.expectPhoto && !form.isBriefing
                          ? 'border-blue-500 bg-blue-50 shadow-sm'
                          : 'border-slate-200 bg-slate-50 hover:border-slate-300 hover:bg-white'"
                        @click="form.expectPhoto = true; form.isBriefing = false"
                      >
                        <span class="text-2xl leading-none">📷</span>
                        <span class="text-sm font-bold" :class="form.expectPhoto && !form.isBriefing ? 'text-blue-700' : 'text-slate-800'">Photo</span>
                        <span class="text-xs text-slate-500 leading-snug">Players send a photo as their answer</span>
                      </button>
                      <button
                        type="button"
                        class="flex flex-col items-center gap-2 px-3 py-4 rounded-xl border-2 transition cursor-pointer text-center"
                        :class="form.isBriefing
                          ? 'border-blue-500 bg-blue-50 shadow-sm'
                          : 'border-slate-200 bg-slate-50 hover:border-slate-300 hover:bg-white'"
                        @click="form.isBriefing = true; form.expectPhoto = false"
                      >
                        <span class="text-2xl leading-none">📋</span>
                        <span class="text-sm font-bold" :class="form.isBriefing ? 'text-blue-700' : 'text-slate-800'">Briefing</span>
                        <span class="text-xs text-slate-500 leading-snug">Instructions + photo, then READY</span>
                      </button>
                    </div>
                  </div>

                  <!-- Intro -->
                  <div class="space-y-1.5">
                    <label class="block text-sm font-semibold text-slate-700">Intro</label>
                    <div class="flex items-center gap-0.5 px-1.5 py-1 bg-slate-50 border border-slate-200 rounded-lg w-fit">
                      <button type="button" title="Bold" class="w-6 h-6 rounded flex items-center justify-center text-xs font-bold text-slate-700 hover:bg-white hover:shadow-sm transition cursor-pointer" @click="applyFormat(introTextarea, 'intro', '<b>', '</b>')">B</button>
                      <button type="button" title="Italic" class="w-6 h-6 rounded flex items-center justify-center text-xs italic text-slate-700 hover:bg-white hover:shadow-sm transition cursor-pointer" @click="applyFormat(introTextarea, 'intro', '<i>', '</i>')">I</button>
                      <button type="button" title="Underline" class="w-6 h-6 rounded flex items-center justify-center text-xs underline text-slate-700 hover:bg-white hover:shadow-sm transition cursor-pointer" @click="applyFormat(introTextarea, 'intro', '<u>', '</u>')">U</button>
                      <button type="button" title="Strikethrough" class="w-6 h-6 rounded flex items-center justify-center text-xs line-through text-slate-700 hover:bg-white hover:shadow-sm transition cursor-pointer" @click="applyFormat(introTextarea, 'intro', '<s>', '</s>')">S</button>
                      <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                      <button type="button" title="Monospace" class="w-7 h-6 rounded flex items-center justify-center font-mono text-[0.6rem] text-slate-700 hover:bg-white hover:shadow-sm transition cursor-pointer" @click="applyFormat(introTextarea, 'intro', '<code>', '</code>')">&lt;/&gt;</button>
                      <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                      <button v-for="c in COLORS" :key="c.hex" type="button" :title="c.label" :style="{ backgroundColor: c.hex }" class="w-3.5 h-3.5 rounded-full cursor-pointer hover:scale-110 transition-transform flex-shrink-0" @click="applyFormat(introTextarea, 'intro', colorTag(c.hex), '</span>')"></button>
                    </div>
                    <textarea
                      ref="introTextarea"
                      v-model="form.intro"
                      rows="2"
                      class="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 resize-none transition bg-white text-slate-900 placeholder-slate-400 font-[inherit]"
                      placeholder="Poem, clue, or context shown before the question…"
                    ></textarea>
                  </div>

                  <!-- Question Text -->
                  <div class="space-y-1.5">
                    <label class="block text-sm font-semibold text-slate-700">
                      Question Text <span class="text-red-500">*</span>
                    </label>
                    <div class="flex items-center gap-0.5 px-1.5 py-1 bg-slate-50 border border-slate-200 rounded-lg w-fit">
                      <button type="button" title="Bold" class="w-6 h-6 rounded flex items-center justify-center text-xs font-bold text-slate-700 hover:bg-white hover:shadow-sm transition cursor-pointer" @click="applyFormat(questionTextarea, 'questionText', '<b>', '</b>')">B</button>
                      <button type="button" title="Italic" class="w-6 h-6 rounded flex items-center justify-center text-xs italic text-slate-700 hover:bg-white hover:shadow-sm transition cursor-pointer" @click="applyFormat(questionTextarea, 'questionText', '<i>', '</i>')">I</button>
                      <button type="button" title="Underline" class="w-6 h-6 rounded flex items-center justify-center text-xs underline text-slate-700 hover:bg-white hover:shadow-sm transition cursor-pointer" @click="applyFormat(questionTextarea, 'questionText', '<u>', '</u>')">U</button>
                      <button type="button" title="Strikethrough" class="w-6 h-6 rounded flex items-center justify-center text-xs line-through text-slate-700 hover:bg-white hover:shadow-sm transition cursor-pointer" @click="applyFormat(questionTextarea, 'questionText', '<s>', '</s>')">S</button>
                      <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                      <button type="button" title="Monospace" class="w-7 h-6 rounded flex items-center justify-center font-mono text-[0.6rem] text-slate-700 hover:bg-white hover:shadow-sm transition cursor-pointer" @click="applyFormat(questionTextarea, 'questionText', '<code>', '</code>')">&lt;/&gt;</button>
                      <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                      <button v-for="c in COLORS" :key="c.hex" type="button" :title="c.label" :style="{ backgroundColor: c.hex }" class="w-3.5 h-3.5 rounded-full cursor-pointer hover:scale-110 transition-transform flex-shrink-0" @click="applyFormat(questionTextarea, 'questionText', colorTag(c.hex), '</span>')"></button>
                    </div>
                    <textarea
                      ref="questionTextarea"
                      v-model="form.questionText"
                      rows="2"
                      required
                      class="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 resize-none transition bg-white text-slate-900 placeholder-slate-400 font-[inherit]"
                      placeholder="Enter the question…"
                    ></textarea>
                  </div>

                  <!-- Question Images -->
                  <div class="space-y-1.5">
                    <label class="block text-sm font-semibold text-slate-700">
                      Question Images <span class="font-normal text-slate-400 text-xs">· up to 4</span>
                    </label>
                    <MultiImageUploadField
                      v-model="form.questionImagePaths"
                      :max="4"
                      @upload="uploadImages('questionImagePaths', $event)"
                    />
                  </div>

                <!-- ── Options & Answer (Quick Reply only) ── -->
                  <template v-if="!form.expectPhoto && !form.isBriefing">
                    <div class="space-y-1.5">
                      <label class="block text-sm font-semibold text-slate-700">Answer Options</label>
                      <div class="space-y-2">
                        <div
                          v-for="(opt, i) in form.options"
                          :key="i"
                          class="flex items-center gap-2 px-3 py-2 rounded-lg border transition"
                          :class="form.answer && form.answer === opt
                            ? 'border-green-300 bg-green-50'
                            : 'border-slate-200 bg-white hover:border-slate-300'"
                        >
                          <span
                            class="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold shrink-0"
                            :class="form.answer && form.answer === opt ? 'bg-green-100 text-green-700' : 'bg-blue-50 text-blue-600'"
                          >{{ String.fromCharCode(65 + i) }}</span>
                          <input
                            v-model="form.options[i]"
                            class="flex-1 text-sm outline-none bg-transparent text-slate-900 placeholder-slate-400"
                            :placeholder="`Option ${String.fromCharCode(65 + i)}`"
                          />
                          <button type="button" class="text-slate-300 hover:text-red-500 transition text-xs px-1 cursor-pointer leading-none" @click="removeOption(i)" title="Remove">✕</button>
                        </div>
                        <button
                          v-if="form.options.length < 4"
                          type="button"
                          class="w-full py-2 border border-dashed border-slate-300 rounded-lg text-xs text-slate-400 hover:border-blue-400 hover:text-blue-500 transition cursor-pointer"
                          @click="addOption"
                        >+ Add option</button>
                      </div>
                    </div>

                    <div class="space-y-1.5">
                      <label class="block text-sm font-semibold text-slate-700">Correct Answer</label>
                      <select
                        v-model="form.answer"
                        class="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 transition bg-white text-slate-900 cursor-pointer"
                      >
                        <option value="">— select —</option>
                        <option v-for="(opt, i) in form.options" :key="i" :value="opt">
                          {{ String.fromCharCode(65 + i) }}: {{ opt || '(empty)' }}
                        </option>
                      </select>
                    </div>
                  </template>

                  <!-- Hint section (not shown for Briefing) -->
                  <div v-if="!form.isBriefing" class="pt-4 border-t border-slate-100 space-y-4">
                    <div class="space-y-1.5">
                      <label class="block text-sm font-semibold text-slate-700">Hint Text</label>
                      <div class="flex items-center gap-0.5 px-1.5 py-1 bg-slate-50 border border-slate-200 rounded-lg w-fit">
                        <button type="button" title="Bold" class="w-6 h-6 rounded flex items-center justify-center text-xs font-bold text-slate-700 hover:bg-white hover:shadow-sm transition cursor-pointer" @click="applyFormat(hintTextarea, 'hintText', '<b>', '</b>')">B</button>
                        <button type="button" title="Italic" class="w-6 h-6 rounded flex items-center justify-center text-xs italic text-slate-700 hover:bg-white hover:shadow-sm transition cursor-pointer" @click="applyFormat(hintTextarea, 'hintText', '<i>', '</i>')">I</button>
                        <button type="button" title="Underline" class="w-6 h-6 rounded flex items-center justify-center text-xs underline text-slate-700 hover:bg-white hover:shadow-sm transition cursor-pointer" @click="applyFormat(hintTextarea, 'hintText', '<u>', '</u>')">U</button>
                        <button type="button" title="Strikethrough" class="w-6 h-6 rounded flex items-center justify-center text-xs line-through text-slate-700 hover:bg-white hover:shadow-sm transition cursor-pointer" @click="applyFormat(hintTextarea, 'hintText', '<s>', '</s>')">S</button>
                        <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                        <button type="button" title="Monospace" class="w-7 h-6 rounded flex items-center justify-center font-mono text-[0.6rem] text-slate-700 hover:bg-white hover:shadow-sm transition cursor-pointer" @click="applyFormat(hintTextarea, 'hintText', '<code>', '</code>')">&lt;/&gt;</button>
                        <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                        <button v-for="c in COLORS" :key="c.hex" type="button" :title="c.label" :style="{ backgroundColor: c.hex }" class="w-3.5 h-3.5 rounded-full cursor-pointer hover:scale-110 transition-transform flex-shrink-0" @click="applyFormat(hintTextarea, 'hintText', colorTag(c.hex), '</span>')"></button>
                      </div>
                      <textarea
                        ref="hintTextarea"
                        v-model="form.hintText"
                        rows="2"
                        class="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 resize-none transition bg-white text-slate-900 placeholder-slate-400 font-[inherit]"
                        placeholder="Hint shown on request…"
                      ></textarea>
                    </div>
                    <div class="space-y-1.5">
                      <label class="block text-sm font-medium text-slate-600">Hint Image <span class="text-slate-400 font-normal text-xs">· up to 4</span></label>
                      <MultiImageUploadField
                        v-model="form.hintImagePaths"
                        :max="4"
                        @upload="uploadImages('hintImagePaths', $event)"
                      />
                    </div>
                  </div>

                </template>

                <!-- ── Tab: Explanation ── -->
                <template v-if="activeTab === 'explanation'">

                  <div class="space-y-1.5">
                    <label class="block text-sm font-semibold text-slate-700">Explanation Texts</label>
                    <div class="space-y-2">
                      <div v-for="(_, i) in form.explanationTexts" :key="i" class="space-y-1">
                        <div class="flex items-center justify-between">
                          <div class="flex items-center gap-0.5 px-1.5 py-1 bg-slate-50 border border-slate-200 rounded-lg w-fit">
                            <button type="button" class="px-1.5 py-0.5 text-xs font-bold text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyExplanationFormat(explanationTextareas[i] ?? null, i, '<b>', '</b>')">B</button>
                            <button type="button" class="px-1.5 py-0.5 text-xs italic text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyExplanationFormat(explanationTextareas[i] ?? null, i, '<i>', '</i>')">I</button>
                            <button type="button" class="px-1.5 py-0.5 text-xs underline text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyExplanationFormat(explanationTextareas[i] ?? null, i, '<u>', '</u>')">U</button>
                            <button type="button" class="px-1.5 py-0.5 text-xs line-through text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyExplanationFormat(explanationTextareas[i] ?? null, i, '<s>', '</s>')">S</button>
                            <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                            <button type="button" class="px-1.5 py-0.5 text-xs font-mono text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyExplanationFormat(explanationTextareas[i] ?? null, i, '<code>', '</code>')">&lt;/&gt;</button>
                            <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                            <button v-for="c in COLORS" :key="c.hex" type="button" :title="c.label" :style="{ backgroundColor: c.hex }" class="w-3.5 h-3.5 rounded-full cursor-pointer hover:scale-110 transition-transform flex-shrink-0" @click="applyExplanationFormat(explanationTextareas[i] ?? null, i, colorTag(c.hex), '</span>')"></button>
                          </div>
                          <button type="button" class="text-slate-300 hover:text-red-500 transition text-xs px-1 cursor-pointer leading-none" @click="removeExplanation(i)" title="Remove">✕</button>
                        </div>
                        <textarea
                          :ref="(el) => { explanationTextareas[i] = el as HTMLTextAreaElement }"
                          v-model="form.explanationTexts[i]"
                          rows="3"
                          class="w-full px-3 py-2 text-sm border border-slate-200 rounded-lg outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 resize-none transition bg-white text-slate-900 placeholder-slate-400 font-[inherit]"
                          :placeholder="`Explanation ${i + 1}…`"
                        ></textarea>
                      </div>
                      <button
                        type="button"
                        class="w-full py-2 border border-dashed border-slate-300 rounded-lg text-xs text-slate-400 hover:border-blue-400 hover:text-blue-500 transition cursor-pointer"
                        @click="addExplanation"
                      >+ Add explanation</button>
                    </div>
                  </div>

                  <div class="space-y-1.5">
                    <label class="block text-sm font-semibold text-slate-700">
                      Explanation Images <span class="font-normal text-slate-400 text-xs">· up to 4</span>
                    </label>
                    <MultiImageUploadField
                      v-model="form.explanationImagePaths"
                      :max="4"
                      @upload="uploadExplanationImages($event)"
                    />
                  </div>

                </template>

              </form>
            </div>

            <!-- Preview panel -->
            <div class="w-1/2 flex flex-col overflow-hidden">

              <!-- Telegram top bar -->
              <div class="flex items-center gap-2 px-3 h-12 shrink-0" style="background: #2b5278">
                <div class="w-8 h-8 rounded-full flex items-center justify-center text-base shrink-0" style="background: #5288c1">🤖</div>
                <div class="flex-1 min-w-0">
                  <div class="text-white text-sm font-semibold leading-none mb-0.5 truncate">Quiz Bot</div>
                  <div class="text-[0.65rem]" style="color: #7fb3e8">bot</div>
                </div>
                <div class="text-white/40 text-base leading-none select-none">⋮</div>
              </div>

              <!-- Chat body -->
              <div class="flex-1 overflow-y-auto px-2 pt-2 pb-3 flex flex-col gap-1" style="background: #e8edf2">

                <!-- Date chip -->
                <div class="flex justify-center mb-1.5">
                  <span class="tg-date-chip">Today</span>
                </div>

                <!-- Intro -->
                <div v-if="form.intro" class="tg-row">
                  <div class="tg-avatar">🤖</div>
                  <div class="tg-msg"><span v-html="tgToHtml(form.intro)"></span><span class="tg-ts">12:00</span></div>
                </div>

                <!-- Question images -->
                <div v-for="(imgId, i) in form.questionImagePaths" :key="'qi'+i" class="tg-row">
                  <div class="tg-avatar">🤖</div>
                  <div class="tg-msg tg-img-msg">
                    <img :src="`/api/files/${imgId}`" class="tg-qimg" alt="" />
                  </div>
                </div>

                <!-- ── Briefing preview ── -->
                <template v-if="form.isBriefing">
                  <div class="tg-row">
                    <div class="tg-avatar">🤖</div>
                    <div class="tg-msg"><span v-html="tgToHtml(form.questionText || 'Your instructions…')"></span><span class="tg-ts">12:00</span></div>
                  </div>
                  <div v-for="(imgId, i) in form.questionImagePaths" :key="'bqi'+i" class="tg-row">
                    <div class="tg-avatar">🤖</div>
                    <div class="tg-msg tg-img-msg">
                      <img :src="`/api/files/${imgId}`" class="tg-qimg" alt="" />
                    </div>
                  </div>
                  <div class="tg-keyboard">
                    <div class="tg-kb-btn tg-kb-ready">▶️ Press READY to begin</div>
                  </div>
                </template>

                <!-- ── Question preview (Quick Reply / Photo) ── -->
                <template v-else>

                <!-- Poll card -->
                <div class="tg-row">
                  <div class="tg-avatar">🤖</div>
                  <div class="tg-msg tg-poll">
                    <div class="tg-poll-number">Question ?/?</div>
                    <div class="tg-poll-q"><span v-html="tgToHtml(form.questionText || 'Your question…')"></span><span class="tg-ts">12:00</span></div>
                  </div>
                </div>

                <!-- Inline keyboard quick replies -->
                <template v-if="!form.expectPhoto && filledOptions.length">
                  <div class="tg-keyboard">
                    <div v-for="(opt, i) in filledOptions" :key="'kb'+i" class="tg-kb-btn">
                      {{ String.fromCharCode(65 + i) }} · {{ opt }}
                    </div>
                    <div v-if="form.hintText" class="tg-kb-btn tg-kb-hint">💡 Hint</div>
                  </div>
                </template>

                <!-- After-hint-click service message -->
                <div v-if="form.hintText" class="flex justify-center my-1.5">
                  <span class="tg-service-msg">after hint button click</span>
                </div>

                <!-- Hint -->
                <div v-if="form.hintText" class="tg-row">
                  <div class="tg-avatar">🤖</div>
                  <div class="tg-msg tg-hint-msg">💡 <span v-html="tgToHtml(form.hintText)"></span><span class="tg-ts">12:01</span></div>
                </div>
                <div v-for="(imgId, i) in form.hintImagePaths" :key="'hi'+i" class="tg-row">
                  <div class="tg-avatar">🤖</div>
                  <div class="tg-msg tg-img-msg">
                    <img :src="`/api/files/${imgId}`" class="tg-qimg" alt="" />
                  </div>
                </div>

                <!-- After-answer service message -->
                <div class="flex justify-center my-1.5">
                  <span class="tg-service-msg">after answer</span>
                </div>

                </template> <!-- end v-else question preview -->

                <!-- Explanation (not shown for briefing) -->
                <template v-if="!form.isBriefing && (filledExplanationTexts.length || filledExplanationImages.length)">
                  <div v-for="(text, i) in filledExplanationTexts" :key="'et'+i" class="tg-row">
                    <div class="tg-avatar">🤖</div>
                    <div class="tg-msg tg-explanation-msg"><span v-html="tgToHtml(text)"></span><span class="tg-ts">12:02</span></div>
                  </div>
                  <div v-for="(img, i) in filledExplanationImages" :key="'ei'+i" class="tg-row">
                    <div class="tg-avatar">🤖</div>
                    <div class="tg-msg tg-img-msg">
                      <img :src="`/api/files/${img}`" class="tg-qimg" alt="" />
                    </div>
                  </div>
                </template>
                <div v-else-if="!form.isBriefing" class="tg-empty-explanation">No explanation added yet…</div>

              </div>
            </div>
          </div>

          <!-- Footer -->
          <div class="flex items-center justify-end gap-2 px-6 py-4 border-t border-slate-100 bg-slate-50/60">
            <span v-if="error" class="text-red-500 text-xs mr-auto">{{ error }}</span>
            <button
              type="button"
              class="px-4 py-2 rounded-xl border border-slate-200 text-sm font-medium text-slate-600 hover:bg-slate-100 transition cursor-pointer"
              @click="$emit('close')"
            >Cancel</button>
            <button
              type="button"
              class="px-5 py-2 rounded-xl bg-blue-600 hover:bg-blue-700 text-white text-sm font-semibold transition disabled:opacity-60 cursor-pointer disabled:cursor-not-allowed inline-flex items-center gap-2"
              :disabled="saving"
              @click="submit"
            >
              <span v-if="saving" class="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin inline-block"></span>
              {{ isEdit ? 'Save changes' : 'Create' }}
            </button>
          </div>

        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup lang="ts">
import { ref, watch, computed, defineComponent, h, nextTick, type PropType } from 'vue'
import type { Question, QuestionRequest } from '@/types/question'
import { questionsService } from '@/services/questionsService'

// ── ImageUploadField (single image) ────────────────────────────────────────
const ImageUploadField = defineComponent({
  name: 'ImageUploadField',
  props: { modelValue: { type: String, default: '' } },
  emits: ['update:modelValue', 'upload'],
  setup(props, { emit }) {
    const fileInput = ref<HTMLInputElement | null>(null)
    const isDragOver = ref(false)
    const uploading = ref(false)

    async function handleFile(file: File) {
      if (!file.type.startsWith('image/')) return
      uploading.value = true
      try {
        emit('upload', file)
      } finally { uploading.value = false }
    }

    async function onFileChange(e: Event) {
      const file = (e.target as HTMLInputElement).files?.[0]
      if (!file) return
      await handleFile(file)
      if (fileInput.value) fileInput.value.value = ''
    }

    return () => {
      if (props.modelValue) {
        return h('div', { class: 'relative rounded-xl overflow-hidden' }, [
          h('img', { src: `/api/files/${props.modelValue}`, class: 'w-full max-h-40 object-cover block rounded-xl', alt: 'preview' }),
          h('button', {
            type: 'button',
            class: 'absolute top-1.5 right-1.5 w-6 h-6 rounded-full bg-black/55 text-white border-none text-[0.7rem] cursor-pointer flex items-center justify-center leading-none hover:bg-red-600/90 transition-colors',
            onClick: () => emit('update:modelValue', ''), title: 'Remove'
          }, '✕')
        ])
      }
      return h('div', {
        class: [
          'border-2 border-dashed rounded-xl p-5 flex items-center justify-center cursor-pointer transition select-none',
          isDragOver.value ? 'border-blue-400 bg-blue-50' : 'border-slate-200 bg-slate-50 hover:border-blue-400 hover:bg-blue-50',
          uploading.value ? 'opacity-60 cursor-wait' : ''
        ],
        onClick: () => !uploading.value && fileInput.value?.click(),
        onDragover: (e: DragEvent) => { e.preventDefault(); isDragOver.value = true },
        onDragleave: () => { isDragOver.value = false },
        onDrop: (e: DragEvent) => {
          e.preventDefault(); isDragOver.value = false
          const f = e.dataTransfer?.files[0]; if (f) handleFile(f)
        }
      }, [
        uploading.value
          ? h('div', { class: 'flex flex-col items-center gap-1 pointer-events-none' }, [
              h('div', { class: 'text-xs text-slate-400' }, 'Uploading…')
            ])
          : h('div', { class: 'flex flex-col items-center gap-1 pointer-events-none' }, [
              h('div', { class: 'text-3xl leading-none' }, '🖼️'),
              h('div', { class: 'text-sm font-semibold text-slate-600' }, 'Drop image here'),
              h('div', { class: 'text-xs text-slate-400' }, 'or click to browse'),
            ]),
        h('input', { ref: fileInput, type: 'file', accept: 'image/*', style: 'display:none', onChange: onFileChange })
      ])
    }
  }
})

// ── MultiImageUploadField (up to N images) ─────────────────────────────────
const MultiImageUploadField = defineComponent({
  name: 'MultiImageUploadField',
  props: {
    modelValue: { type: Array as PropType<string[]>, default: () => [] },
    max: { type: Number, default: 4 }
  },
  emits: ['update:modelValue', 'upload'],
  setup(props, { emit }) {
    const fileInput = ref<HTMLInputElement | null>(null)
    const isDragOver = ref(false)
    const uploading = ref(false)

    async function handleFiles(rawFiles: FileList | File[]) {
      const valid = Array.from(rawFiles).filter(f => f.type.startsWith('image/'))
      const slots = props.max - props.modelValue.length
      const toProcess = valid.slice(0, slots)
      if (!toProcess.length) return
      uploading.value = true
      try {
        emit('upload', toProcess)
      } finally { uploading.value = false }
    }

    function removeImage(i: number) {
      const updated = [...props.modelValue]
      updated.splice(i, 1)
      emit('update:modelValue', updated)
    }

    return () => {
      const canAdd = props.modelValue.length < props.max
      const thumbnails = props.modelValue.map((path, i) =>
        h('div', { key: i, class: 'aspect-square rounded-lg overflow-hidden relative border border-slate-200' }, [
          h('img', { src: `/api/files/${path}`, class: 'w-full h-full object-cover block', alt: '' }),
          h('button', {
            type: 'button',
            class: 'absolute top-1 right-1 w-5 h-5 rounded-full bg-black/55 text-white border-none text-[0.65rem] cursor-pointer flex items-center justify-center leading-none hover:bg-red-600/90 transition-colors',
            onClick: () => removeImage(i), title: 'Remove'
          }, '✕')
        ])
      )
      const addCell = canAdd
        ? h('div', {
            key: 'add',
            class: [
              'aspect-square rounded-lg border-2 border-dashed flex flex-col items-center justify-center gap-1 cursor-pointer transition select-none',
              isDragOver.value ? 'border-blue-400 bg-blue-50' : 'border-slate-300 bg-slate-50 hover:border-blue-400 hover:bg-blue-50'
            ],
            onClick: () => !uploading.value && fileInput.value?.click(),
            onDragover: (e: DragEvent) => { e.preventDefault(); isDragOver.value = true },
            onDragleave: () => { isDragOver.value = false },
            onDrop: (e: DragEvent) => {
              e.preventDefault(); isDragOver.value = false
              if (e.dataTransfer?.files) handleFiles(e.dataTransfer.files)
            }
          }, uploading.value
            ? [h('div', { class: 'text-[0.7rem] text-slate-400' }, 'Uploading…')]
            : [
                h('div', { class: 'text-xl text-slate-400 leading-none' }, props.modelValue.length === 0 ? '🖼️' : '+'),
                h('div', { class: 'text-[0.7rem] text-slate-400 text-center leading-tight' },
                  props.modelValue.length === 0 ? 'Drop or click to add' : `${props.modelValue.length} / ${props.max}`),
              ]
          )
        : null
      return h('div', { class: 'grid grid-cols-4 gap-2' }, [
        ...thumbnails,
        addCell,
        h('input', {
          ref: fileInput, type: 'file', accept: 'image/*', multiple: true,
          style: 'display:none',
          onChange: (e: Event) => {
            const files = (e.target as HTMLInputElement).files
            if (files) handleFiles(files)
            if (fileInput.value) fileInput.value.value = ''
          }
        })
      ])
    }
  }
})

// ── Props / emits ──────────────────────────────────────────────────────────
const props = defineProps<{
  visible: boolean
  question: Question | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'saved', q: Question): void
}>()

// ── Form state ─────────────────────────────────────────────────────────────
function blankForm() {
  return {
    questionText: '',
    options: [] as string[],
    answer: '',
    expectPhoto: false,
    isBriefing: false,
    intro: '',
    introBlue: true,
    questionImagePaths: [] as string[],
    hintText: '',
    hintImagePaths: [] as string[],
    explanationTexts: [] as string[],
    explanationImagePaths: [] as string[],
  }
}

const form = ref(blankForm())
const saving = ref(false)
const error = ref('')
const isEdit = ref(false)
const activeTab = ref<'question' | 'explanation'>('question')
const tabs = [
  { value: 'question' as const, label: 'Question' },
  { value: 'explanation' as const, label: 'Explanation' },
]
const filledOptions = computed(() => form.value.options.filter(o => o.trim()))
const filledExplanationTexts = computed(() => form.value.explanationTexts.filter(t => t.trim()))
const filledExplanationImages = computed(() => form.value.explanationImagePaths.filter(p => p.trim()))

// ── Rich text formatting ───────────────────────────────────────────────────
const introTextarea = ref<HTMLTextAreaElement | null>(null)
const questionTextarea = ref<HTMLTextAreaElement | null>(null)
const hintTextarea = ref<HTMLTextAreaElement | null>(null)
const explanationTextareas = ref<(HTMLTextAreaElement | null)[]>([])

const COLORS = [
  { label: 'Red',    hex: '#e53935' },
  { label: 'Orange', hex: '#fb8c00' },
  { label: 'Yellow', hex: '#fdd835' },
  { label: 'Green',  hex: '#43a047' },
  { label: 'Cyan',   hex: '#00acc1' },
  { label: 'Blue',   hex: '#1e88e5' },
  { label: 'Purple', hex: '#8e24aa' },
]

function colorTag(hex: string): string {
  return `<span style="color: ${hex}">`
}

// Shared smart toggle: handles both plain tags and color spans
function toggleFormat(text: string, start: number, end: number, open: string, close: string): { text: string, selStart: number, selEnd: number } {
  const isColor = open.startsWith('<span style="color:')
  if (isColor) {
    // Check if selection is immediately inside any color span
    if (text.slice(end, end + close.length) === close) {
      const before = text.slice(0, start)
      const match = before.match(/<span style="color: [^"]+">$/)
      if (match) {
        const existingOpen = match[0]
        const preLen = existingOpen.length
        if (existingOpen === open) {
          // Same colour — strip
          const newText = text.slice(0, start - preLen) + text.slice(start, end) + text.slice(end + close.length)
          return { text: newText, selStart: start - preLen, selEnd: end - preLen }
        } else {
          // Different colour — replace
          const newText = text.slice(0, start - preLen) + open + text.slice(start, end) + close + text.slice(end + close.length)
          return { text: newText, selStart: start - preLen + open.length, selEnd: end - preLen + open.length }
        }
      }
    }
    // No existing colour — wrap
    const newText = text.slice(0, start) + open + text.slice(start, end) + close + text.slice(end)
    return { text: newText, selStart: start + open.length, selEnd: end + open.length }
  }

  // Plain tag toggle
  if (start >= open.length && text.slice(start - open.length, start) === open && text.slice(end, end + close.length) === close) {
    const newText = text.slice(0, start - open.length) + text.slice(start, end) + text.slice(end + close.length)
    return { text: newText, selStart: start - open.length, selEnd: end - open.length }
  }
  const newText = text.slice(0, start) + open + text.slice(start, end) + close + text.slice(end)
  return { text: newText, selStart: start + open.length, selEnd: end + open.length }
}

function applyFormat(el: HTMLTextAreaElement | HTMLInputElement | null, field: 'intro' | 'questionText' | 'hintText', open: string, close: string) {
  if (!el) return
  const start = el.selectionStart ?? 0
  const end = el.selectionEnd ?? 0
  const result = toggleFormat(form.value[field], start, end, open, close)
  form.value[field] = result.text
  nextTick(() => { el.focus(); el.setSelectionRange(result.selStart, result.selEnd) })
}

function applyExplanationFormat(el: HTMLTextAreaElement | null, index: number, open: string, close: string) {
  if (!el) return
  const start = el.selectionStart ?? 0
  const end = el.selectionEnd ?? 0
  const result = toggleFormat(form.value.explanationTexts[index], start, end, open, close)
  form.value.explanationTexts[index] = result.text
  nextTick(() => { el.focus(); el.setSelectionRange(result.selStart, result.selEnd) })
}

function tgToHtml(text: string): string {
  if (!text) return ''
  return text.replace(/\n/g, '<br>')
}

watch(() => props.visible, (v) => {
  if (!v) return
  error.value = ''
  activeTab.value = 'question'
  if (props.question) {
    const q = props.question
    isEdit.value = true
    form.value = {
      questionText: q.questionText,
      options: [...q.options],
      answer: q.answer ?? '',
      expectPhoto: q.expectPhoto,
      isBriefing: q.isBriefing,
      intro: q.intro ?? '',
      introBlue: q.introBlue,
      questionImagePaths: [...q.questionImagePaths],
      hintText: q.hintText ?? '',
      hintImagePaths: [...q.hintImagePaths],
      explanationTexts: [...q.explanationTexts],
      explanationImagePaths: [...q.explanationImagePaths],
    }
  } else {
    isEdit.value = false
    form.value = blankForm()
  }
})

// ── Helpers ────────────────────────────────────────────────────────────────
function addOption() { form.value.options.push('') }
function removeOption(i: number) {
  const removed = form.value.options.splice(i, 1)[0]
  if (form.value.answer === removed) form.value.answer = ''
}

function addExplanation() { form.value.explanationTexts.push('') }
function removeExplanation(i: number) { form.value.explanationTexts.splice(i, 1) }

async function uploadImages(field: 'questionImagePaths' | 'hintImagePaths', files: File[]) {
  for (const file of files) {
    try {
      const path = await questionsService.uploadFile(file)
      form.value[field].push(path)
    } catch {
      error.value = 'Image upload failed.'
    }
  }
}

async function uploadExplanationImages(files: File[]) {
  for (const file of files) {
    try {
      const path = await questionsService.uploadFile(file)
      form.value.explanationImagePaths.push(path)
    } catch {
      error.value = 'Image upload failed.'
    }
  }
}

async function submit() {
  if (!form.value.questionText.trim()) {
    error.value = 'Question text is required.'
    return
  }
  error.value = ''
  saving.value = true

  const payload: QuestionRequest = {
    questionText: form.value.questionText.trim(),
    options: (form.value.expectPhoto || form.value.isBriefing) ? [] : form.value.options.filter(o => o.trim()),
    answer: form.value.isBriefing ? null : (form.value.answer || null),
    expectPhoto: form.value.expectPhoto,
    isBriefing: form.value.isBriefing,
    intro: form.value.intro || null,
    introBlue: form.value.introBlue,
    questionImagePaths: form.value.questionImagePaths.filter(p => p),
    hintText: form.value.hintText || null,
    hintImagePaths: form.value.hintImagePaths.filter(p => p),
    explanationTexts: form.value.explanationTexts.filter(t => t.trim()),
    explanationImagePaths: form.value.explanationImagePaths.filter(p => p),
  }

  try {
    const saved = isEdit.value && props.question
      ? await questionsService.update(props.question.id, payload)
      : await questionsService.create(payload)
    emit('saved', saved)
  } catch {
    error.value = 'Failed to save question. Please try again.'
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
/* ── Date / service chips ── */
.tg-date-chip, .tg-service-msg {
  font-size: 0.6rem;
  color: rgba(255,255,255,0.9);
  background: rgba(0,0,0,0.22);
  border-radius: 10px;
  padding: 0.2rem 0.6rem;
  white-space: nowrap;
}
.tg-service-msg { font-style: italic; }

/* ── Message row ── */
.tg-row {
  display: flex;
  align-items: flex-end;
  gap: 4px;
}
.tg-avatar {
  width: 26px;
  height: 26px;
  min-width: 26px;
  border-radius: 50%;
  background: #5288c1;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.8rem;
  flex-shrink: 0;
}

.tg-msg {
  background: #fff;
  border-radius: 2px 12px 12px 12px;
  padding: 0.4rem 0.55rem 0.3rem;
  font-size: 0.8rem;
  color: #111;
  box-shadow: 0 1px 2px rgba(0,0,0,0.08);
  word-break: break-word;
  line-height: 1.45;
  position: relative;
  max-width: 82%;
  min-width: 0;
  overflow: hidden;
}

/* Timestamp ── floated to bottom-right inside bubble */
.tg-ts {
  font-size: 0.6rem;
  color: #8d9eac;
  float: right;
  margin-left: 5px;
  margin-top: 3px;
  line-height: 1;
  display: inline-block;
}

.tg-intro-blue { }

.tg-img-msg { padding: 0.25rem; max-width: 240px; }
.tg-qimg {
  width: 100%;
  border-radius: 10px;
  display: block;
  max-height: 180px;
  object-fit: cover;
}

.tg-poll { padding: 0.55rem 0.65rem 0.4rem; min-width: 175px; }

.tg-poll-number {
  font-size: 0.72rem;
  font-weight: 700;
  color: #111;
  margin-bottom: 0.3rem;
}

.tg-poll-header {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  margin-bottom: 0.5rem;
}
.tg-poll-icon { font-size: 0.85rem; line-height: 1; }
.tg-poll-type {
  font-size: 0.6rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: #4fabe2;
}

.tg-poll-q {
  font-size: 0.875rem;
  font-weight: normal;
  color: #111;
  margin-bottom: 0.45rem;
  line-height: 1.35;
}

.tg-photo-note {
  font-size: 0.8125rem;
  color: #6b7280;
  font-style: italic;
  padding: 0.5rem 0;
}

.tg-empty-opts {
  font-size: 0.8rem;
  color: #9ca3af;
  font-style: italic;
  padding: 0.25rem 0 0.5rem;
}

.tg-opt { margin-bottom: 0.375rem; }
.tg-opt-row {
  display: flex;
  align-items: center;
  gap: 0.375rem;
  font-size: 0.875rem;
  margin-bottom: 0.2rem;
}
.tg-opt-letter {
  font-size: 0.62rem;
  font-weight: 700;
  color: #4fabe2;
  background: #e8f4fd;
  border-radius: 50%;
  width: 15px;
  height: 15px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.tg-opt-text { flex: 1; color: #111; font-size: 0.78rem; }
.tg-opt-pct { font-size: 0.62rem; color: #8d9eac; flex-shrink: 0; min-width: 22px; text-align: right; }
.tg-opt-bar { height: 2px; background: #e8edf2; border-radius: 1px; overflow: hidden; }
.tg-opt-fill { height: 100%; width: 0%; background: #4fabe2; border-radius: 1px; }

.tg-poll-meta {
  font-size: 0.62rem;
  color: #8d9eac;
  margin-top: 0.35rem;
  padding-top: 0.35rem;
  border-top: 1px solid #f0f4f8;
}

.tg-vote-btn {
  display: none; /* replaced by inline keyboard */
}

.tg-keyboard {
  display: flex;
  flex-direction: column;
  gap: 3px;
  margin-top: 1px;
  padding-left: 30px;
  max-width: 90%;
}

.tg-kb-btn {
  background: rgba(255,255,255,0.92);
  border-radius: 8px;
  padding: 0.35rem 0.6rem;
  font-size: 0.78rem;
  color: #2196f3;
  font-weight: 500;
  text-align: center;
  box-shadow: 0 1px 2px rgba(0,0,0,0.07);
  cursor: default;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.tg-kb-hint { color: #f59e0b; }
.tg-kb-ready { color: #16a34a; font-weight: 700; }
.tg-hint-msg { color: #374151; }
.tg-explanation-msg { color: #111; }

.tg-empty-explanation {
  font-size: 0.72rem;
  color: #9ca3af;
  font-style: italic;
  text-align: center;
  padding: 0.5rem 0;
}
</style>
