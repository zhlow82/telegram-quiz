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
        <div class="absolute inset-0 bg-black/50" />

        <!-- Modal panel -->
        <div
          class="relative bg-white rounded-2xl shadow-2xl flex flex-col md:flex-row w-full overflow-hidden"
          style="width: 90vw; max-width: 90vw; height: 90vh; max-height: 90vh"
          role="dialog"
          aria-modal="true"
        >

          <!-- Left side: Header + Form + Footer -->
          <div class="w-full md:w-1/2 flex flex-col overflow-hidden">

            <!-- Header -->
            <div class="flex items-center justify-between px-6 py-4 border-b border-slate-100 shrink-0">
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

            <!-- Form panel -->
            <div class="flex-1 overflow-y-auto tab-scroll">

              <!-- Question Type (always visible) -->
              <div class="px-6 pt-5 pb-4 border-b border-slate-100 shrink-0">
                <div class="space-y-2">
                  <label class="block text-sm font-semibold text-slate-700">Question Type</label>
                  <div class="grid grid-cols-2 sm:grid-cols-4 gap-3">

                    <!-- Multiple Choice -->
                    <button
                      type="button"
                      class="flex flex-col items-center gap-2 px-3 py-4 rounded-xl border-2 transition cursor-pointer text-center"
                      :class="!form.expectPhoto && !form.isBriefing && !form.expectsTextInput
                        ? 'border-blue-500 bg-blue-50 shadow-md'
                        : 'border-slate-200 bg-white hover:border-blue-200 hover:bg-blue-50/40'"
                      @click="setQuestionType(false, false)"
                    >
                      <div
                        class="w-10 h-10 rounded-xl flex items-center justify-center transition"
                        :class="!form.expectPhoto && !form.isBriefing && !form.expectsTextInput
                          ? 'bg-blue-600 text-white shadow-sm'
                          : 'bg-slate-100 text-slate-400'"
                      >
                        <ListChecks class="w-5 h-5" />
                      </div>
                      <span class="text-sm font-bold leading-tight" :class="!form.expectPhoto && !form.isBriefing && !form.expectsTextInput ? 'text-blue-700' : 'text-slate-700'">Multiple Choice</span>
                      <span class="text-xs text-slate-500 leading-snug">Players pick from A / B / C / D options</span>
                    </button>

                    <!-- Photo -->
                    <button
                      type="button"
                      class="flex flex-col items-center gap-2 px-3 py-4 rounded-xl border-2 transition cursor-pointer text-center"
                      :class="form.expectPhoto && !form.isBriefing
                        ? 'border-violet-500 bg-violet-50 shadow-md'
                        : 'border-slate-200 bg-white hover:border-violet-200 hover:bg-violet-50/40'"
                      @click="setQuestionType(true, false)"
                    >
                      <div
                        class="w-10 h-10 rounded-xl flex items-center justify-center transition"
                        :class="form.expectPhoto && !form.isBriefing
                          ? 'bg-violet-600 text-white shadow-sm'
                          : 'bg-slate-100 text-slate-400'"
                      >
                        <Camera class="w-5 h-5" />
                      </div>
                      <span class="text-sm font-bold leading-tight" :class="form.expectPhoto && !form.isBriefing ? 'text-violet-700' : 'text-slate-700'">Photo</span>
                      <span class="text-xs text-slate-500 leading-snug">Players send a photo as their answer</span>
                    </button>

                    <!-- Briefing -->
                    <button
                      type="button"
                      class="flex flex-col items-center gap-2 px-3 py-4 rounded-xl border-2 transition cursor-pointer text-center"
                      :class="form.isBriefing
                        ? 'border-amber-500 bg-amber-50 shadow-md'
                        : 'border-slate-200 bg-white hover:border-amber-200 hover:bg-amber-50/40'"
                      @click="setQuestionType(false, true)"
                    >
                      <div
                        class="w-10 h-10 rounded-xl flex items-center justify-center transition"
                        :class="form.isBriefing
                          ? 'bg-amber-500 text-white shadow-sm'
                          : 'bg-slate-100 text-slate-400'"
                      >
                        <BookOpen class="w-5 h-5" />
                      </div>
                      <span class="text-sm font-bold leading-tight" :class="form.isBriefing ? 'text-amber-700' : 'text-slate-700'">Briefing</span>
                      <span class="text-xs text-slate-500 leading-snug">Instructions + photo, then READY</span>
                    </button>

                    <!-- Text Input -->
                    <button
                      type="button"
                      class="flex flex-col items-center gap-2 px-3 py-4 rounded-xl border-2 transition cursor-pointer text-center"
                      :class="form.expectsTextInput
                        ? 'border-emerald-500 bg-emerald-50 shadow-md'
                        : 'border-slate-200 bg-white hover:border-emerald-200 hover:bg-emerald-50/40'"
                      @click="setQuestionType(false, false, true)"
                    >
                      <div
                        class="w-10 h-10 rounded-xl flex items-center justify-center transition"
                        :class="form.expectsTextInput
                          ? 'bg-emerald-600 text-white shadow-sm'
                          : 'bg-slate-100 text-slate-400'"
                      >
                        <Type class="w-5 h-5" />
                      </div>
                      <span class="text-sm font-bold leading-tight" :class="form.expectsTextInput ? 'text-emerald-700' : 'text-slate-700'">Text Input</span>
                      <span class="text-xs text-slate-500 leading-snug">Players type a short answer like team name</span>
                    </button>

                  </div>
                </div>
              </div>

              <!-- Folder picker -->
              <div v-if="props.folders.length > 0" class="flex items-center gap-3 px-5 py-2 border-b border-slate-100 bg-slate-50/40 shrink-0">
                <span class="text-[0.7rem] font-semibold text-slate-400 uppercase tracking-wide flex-shrink-0">Folder</span>
                <select
                  v-model="form.folderId"
                  class="flex-1 text-xs border border-slate-200 rounded-lg px-2 py-1.5 bg-white text-slate-700 outline-none focus:border-blue-400 cursor-pointer transition"
                >
                  <option :value="null">— No folder —</option>
                  <option v-for="f in props.folders" :key="f.id" :value="f.id">{{ f.name }}</option>
                </select>
              </div>

              <!-- Sub-tabs -->
              <div class="flex gap-1 border-b border-slate-200 px-4 pt-1 shrink-0">
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

              <!-- Tab content -->
              <div ref="tabContentRef" class="flex-1">
                <form @submit.prevent="submit" class="p-6 space-y-5">

                  <!-- ── Tab: Question (Multiple Choice / Photo) ── -->
                  <template v-if="activeTab === 'question'">

                    <!-- Question Blocks -->
                    <div class="space-y-1.5">
                      <label class="block text-sm font-semibold text-slate-700">
                        Question Content <span class="text-red-500">*</span>
                      </label>
                      <VueDraggable v-model="form.questionBlocks" :animation="150" handle=".drag-handle" class="space-y-2">
                        <div v-for="block in form.questionBlocks" :key="block._id" class="rounded-lg border border-slate-200 bg-white overflow-hidden">
                          <div class="flex items-center gap-1.5 px-2.5 py-1.5 bg-slate-50 border-b border-slate-100">
                            <div class="drag-handle cursor-grab text-slate-300 hover:text-slate-500 select-none text-base leading-none px-0.5" title="Drag to reorder">⠿</div>
                            <span class="text-xs font-semibold" :class="block.type === 'text' ? 'text-blue-600' : 'text-amber-600'">{{ block.type === 'text' ? 'Text' : 'Image' }}</span>
                            <div class="flex-1"></div>
                            <button type="button" class="text-slate-300 hover:text-red-500 transition text-xs leading-none cursor-pointer px-1" @click="removeBlock(form.questionBlocks, block._id)" title="Remove">✕</button>
                          </div>
                          <div v-if="block.type === 'text'" class="p-3 space-y-1.5">
                            <div class="flex items-center gap-0.5 px-1.5 py-1 bg-slate-50 border border-slate-200 rounded-lg w-fit">
                              <button type="button" class="px-1.5 py-0.5 text-xs font-bold text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<b>', '</b>')">B</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs italic text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<i>', '</i>')">I</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs underline text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<u>', '</u>')">U</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs line-through text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<s>', '</s>')">S</button>
                              <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                              <button type="button" class="px-1.5 py-0.5 text-xs font-mono text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<code>', '</code>')">&lt;/&gt;</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" title="Bullet" @click="insertBullet(questionTextareas[block._id] ?? null, form.questionBlocks, block._id)">•</button>
                              <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                              <button v-for="c in COLORS" :key="c.hex" type="button" :title="c.label" :style="{ backgroundColor: c.hex }" class="w-3.5 h-3.5 rounded-full cursor-pointer hover:scale-110 transition-transform flex-shrink-0" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, colorTag(c.hex), '</span>')"></button>
                            </div>
                            <textarea
                              :ref="(el) => { questionTextareas[block._id] = el as HTMLTextAreaElement }"
                              v-model="block.content"
                              class="tg-auto-textarea w-full px-3 py-2 text-sm border border-slate-200 rounded-lg outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 resize-none transition bg-white text-slate-900 placeholder-slate-400 font-[inherit]"
                              placeholder="Type here…"
                            ></textarea>
                          </div>
                          <div v-else class="p-3">
                            <div v-if="block.content" class="relative rounded-lg overflow-hidden">
                              <img :src="`/api/files/${block.content}`" class="max-h-48 w-full object-contain block rounded-lg bg-slate-100" alt="" />
                              <button type="button" class="absolute top-1.5 right-1.5 w-6 h-6 rounded-full bg-black/55 text-white border-none text-[0.7rem] cursor-pointer flex items-center justify-center leading-none hover:bg-red-600/90 transition-colors" @click="block.content = ''" title="Remove">✕</button>
                            </div>
                            <div v-else class="border-2 border-dashed rounded-xl p-5 flex items-center justify-center cursor-pointer transition select-none border-slate-200 bg-slate-50 hover:border-amber-400 hover:bg-amber-50" @click="uploadImageBlock(block)">
                              <div class="flex flex-col items-center gap-1 pointer-events-none">
                                <div class="text-3xl leading-none">🖼️</div>
                                <div class="text-sm font-semibold text-slate-600">Click to upload</div>
                                <div class="text-xs text-slate-400">or drag &amp; drop a file</div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </VueDraggable>
                      <div class="flex gap-2">
                        <button type="button" class="flex-1 py-2 border border-dashed border-slate-300 rounded-lg text-xs text-slate-400 hover:border-blue-400 hover:text-blue-500 transition cursor-pointer" @click="addTextBlock(form.questionBlocks)">+ Add text</button>
                        <button type="button" class="flex-1 py-2 border border-dashed border-amber-200 rounded-lg text-xs text-amber-400 hover:border-amber-400 hover:text-amber-500 transition cursor-pointer" @click="addImageBlock(form.questionBlocks)">+ Add image</button>
                      </div>
                    </div>

                  </template>

                  <!-- ── Tab: Answer (Multiple Choice only) ── -->
                  <template v-if="activeTab === 'answer'">

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

                  <!-- ── Tab: Points ── -->
                  <template v-if="activeTab === 'mark'">

                    <div class="space-y-4">
                      <div class="space-y-1.5">
                        <label class="block text-sm font-semibold text-slate-700">Points</label>
                        <p class="text-xs text-slate-400">How many points this question is worth. Leave blank for unscored.</p>
                        <div class="flex items-center gap-3">
                          <input
                            :value="form.mark ?? ''"
                            type="number"
                            min="0"
                            step="1"
                            class="w-32 px-3 py-2 text-sm border border-slate-200 rounded-lg outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 transition bg-white text-slate-900 placeholder-slate-400"
                            placeholder="e.g. 1"
                            @input="onMarkInput"
                          />
                          <button
                            v-if="form.mark !== null"
                            type="button"
                            class="text-xs text-slate-400 hover:text-red-500 transition cursor-pointer"
                            @click="form.mark = null"
                          >Clear</button>
                        </div>
                      </div>
                      <div class="flex flex-wrap gap-2">
                        <span class="text-xs text-slate-500 self-center">Quick set:</span>
                        <button v-for="n in [0, 1, 2, 3, 5, 10]" :key="n" type="button"
                          class="px-3 py-1 rounded-full text-xs font-semibold border transition cursor-pointer"
                          :class="form.mark === n
                            ? 'bg-blue-600 text-white border-blue-600'
                            : 'bg-white text-slate-600 border-slate-200 hover:border-blue-400 hover:text-blue-600'"
                          @click="form.mark = n"
                        >{{ n }}</button>
                      </div>
                    </div>

                  </template>

                  <!-- ── Tab: Hint ── -->
                  <template v-if="activeTab === 'hint'">

                    <div class="space-y-1.5">
                      <label class="block text-sm font-semibold text-slate-700">Hint Content</label>
                      <VueDraggable v-model="form.hintBlocks" :animation="150" handle=".drag-handle" class="space-y-2">
                        <div v-for="block in form.hintBlocks" :key="block._id" class="rounded-lg border border-slate-200 bg-white overflow-hidden">
                          <div class="flex items-center gap-1.5 px-2.5 py-1.5 bg-slate-50 border-b border-slate-100">
                            <div class="drag-handle cursor-grab text-slate-300 hover:text-slate-500 select-none text-base leading-none px-0.5" title="Drag to reorder">⠿</div>
                            <span class="text-xs font-semibold" :class="block.type === 'text' ? 'text-blue-600' : 'text-amber-600'">{{ block.type === 'text' ? 'Text' : 'Image' }}</span>
                            <div class="flex-1"></div>
                            <button type="button" class="text-slate-300 hover:text-red-500 transition text-xs leading-none cursor-pointer px-1" @click="removeBlock(form.hintBlocks, block._id)" title="Remove">✕</button>
                          </div>
                          <div v-if="block.type === 'text'" class="p-3 space-y-1.5">
                            <div class="flex items-center gap-0.5 px-1.5 py-1 bg-slate-50 border border-slate-200 rounded-lg w-fit">
                              <button type="button" class="px-1.5 py-0.5 text-xs font-bold text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(hintTextareas[block._id] ?? null, form.hintBlocks, block._id, '<b>', '</b>')">B</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs italic text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(hintTextareas[block._id] ?? null, form.hintBlocks, block._id, '<i>', '</i>')">I</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs underline text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(hintTextareas[block._id] ?? null, form.hintBlocks, block._id, '<u>', '</u>')">U</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs line-through text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(hintTextareas[block._id] ?? null, form.hintBlocks, block._id, '<s>', '</s>')">S</button>
                              <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                              <button type="button" class="px-1.5 py-0.5 text-xs font-mono text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(hintTextareas[block._id] ?? null, form.hintBlocks, block._id, '<code>', '</code>')">&lt;/&gt;</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" title="Bullet" @click="insertBullet(hintTextareas[block._id] ?? null, form.hintBlocks, block._id)">•</button>
                              <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                              <button v-for="c in COLORS" :key="c.hex" type="button" :title="c.label" :style="{ backgroundColor: c.hex }" class="w-3.5 h-3.5 rounded-full cursor-pointer hover:scale-110 transition-transform flex-shrink-0" @click="applyBlockFormat(hintTextareas[block._id] ?? null, form.hintBlocks, block._id, colorTag(c.hex), '</span>')"></button>
                            </div>
                            <textarea
                              :ref="(el) => { hintTextareas[block._id] = el as HTMLTextAreaElement }"
                              v-model="block.content"
                              class="tg-auto-textarea w-full px-3 py-2 text-sm border border-slate-200 rounded-lg outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 resize-none transition bg-white text-slate-900 placeholder-slate-400 font-[inherit]"
                              placeholder="Hint text…"
                            ></textarea>
                          </div>
                          <div v-else class="p-3">
                            <div v-if="block.content" class="relative rounded-lg overflow-hidden">
                              <img :src="`/api/files/${block.content}`" class="max-h-48 w-full object-contain block rounded-lg bg-slate-100" alt="" />
                              <button type="button" class="absolute top-1.5 right-1.5 w-6 h-6 rounded-full bg-black/55 text-white border-none text-[0.7rem] cursor-pointer flex items-center justify-center leading-none hover:bg-red-600/90 transition-colors" @click="block.content = ''" title="Remove">✕</button>
                            </div>
                            <div v-else class="border-2 border-dashed rounded-xl p-5 flex items-center justify-center cursor-pointer transition select-none border-slate-200 bg-slate-50 hover:border-amber-400 hover:bg-amber-50" @click="uploadImageBlock(block)">
                              <div class="flex flex-col items-center gap-1 pointer-events-none">
                                <div class="text-3xl leading-none">🖼️</div>
                                <div class="text-sm font-semibold text-slate-600">Click to upload</div>
                                <div class="text-xs text-slate-400">or drag &amp; drop a file</div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </VueDraggable>
                      <div class="flex gap-2">
                        <button type="button" class="flex-1 py-2 border border-dashed border-slate-300 rounded-lg text-xs text-slate-400 hover:border-blue-400 hover:text-blue-500 transition cursor-pointer" @click="addTextBlock(form.hintBlocks)">+ Add text</button>
                        <button type="button" class="flex-1 py-2 border border-dashed border-amber-200 rounded-lg text-xs text-amber-400 hover:border-amber-400 hover:text-amber-500 transition cursor-pointer" @click="addImageBlock(form.hintBlocks)">+ Add image</button>
                      </div>
                    </div>

                  </template>

                  <!-- ── Tab: After Answer ── -->
                  <template v-if="activeTab === 'explanation'">

                    <div class="space-y-1.5">
                      <label class="block text-sm font-semibold text-slate-700">After Answer Content</label>
                      <VueDraggable v-model="form.explanationBlocks" :animation="150" handle=".drag-handle" class="space-y-2">
                        <div v-for="block in form.explanationBlocks" :key="block._id" class="rounded-lg border border-slate-200 bg-white overflow-hidden">
                          <div class="flex items-center gap-1.5 px-2.5 py-1.5 bg-slate-50 border-b border-slate-100">
                            <div class="drag-handle cursor-grab text-slate-300 hover:text-slate-500 select-none text-base leading-none px-0.5" title="Drag to reorder">⠿</div>
                            <span class="text-xs font-semibold" :class="block.type === 'text' ? 'text-blue-600' : 'text-amber-600'">{{ block.type === 'text' ? 'Text' : 'Image' }}</span>
                            <div class="flex-1"></div>
                            <button type="button" class="text-slate-300 hover:text-red-500 transition text-xs leading-none cursor-pointer px-1" @click="removeBlock(form.explanationBlocks, block._id)" title="Remove">✕</button>
                          </div>
                          <div v-if="block.type === 'text'" class="p-3 space-y-1.5">
                            <div class="flex items-center gap-0.5 px-1.5 py-1 bg-slate-50 border border-slate-200 rounded-lg w-fit">
                              <button type="button" class="px-1.5 py-0.5 text-xs font-bold text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(explanationTextareas[block._id] ?? null, form.explanationBlocks, block._id, '<b>', '</b>')">B</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs italic text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(explanationTextareas[block._id] ?? null, form.explanationBlocks, block._id, '<i>', '</i>')">I</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs underline text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(explanationTextareas[block._id] ?? null, form.explanationBlocks, block._id, '<u>', '</u>')">U</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs line-through text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(explanationTextareas[block._id] ?? null, form.explanationBlocks, block._id, '<s>', '</s>')">S</button>
                              <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                              <button type="button" class="px-1.5 py-0.5 text-xs font-mono text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(explanationTextareas[block._id] ?? null, form.explanationBlocks, block._id, '<code>', '</code>')">&lt;/&gt;</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" title="Bullet" @click="insertBullet(explanationTextareas[block._id] ?? null, form.explanationBlocks, block._id)">•</button>
                              <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                              <button v-for="c in COLORS" :key="c.hex" type="button" :title="c.label" :style="{ backgroundColor: c.hex }" class="w-3.5 h-3.5 rounded-full cursor-pointer hover:scale-110 transition-transform flex-shrink-0" @click="applyBlockFormat(explanationTextareas[block._id] ?? null, form.explanationBlocks, block._id, colorTag(c.hex), '</span>')"></button>
                            </div>
                            <textarea
                              :ref="(el) => { explanationTextareas[block._id] = el as HTMLTextAreaElement }"
                              v-model="block.content"
                              class="tg-auto-textarea w-full px-3 py-2 text-sm border border-slate-200 rounded-lg outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 resize-none transition bg-white text-slate-900 placeholder-slate-400 font-[inherit]"
                              placeholder="After answer text…"
                            ></textarea>
                          </div>
                          <div v-else class="p-3">
                            <div v-if="block.content" class="relative rounded-lg overflow-hidden">
                              <img :src="`/api/files/${block.content}`" class="max-h-48 w-full object-contain block rounded-lg bg-slate-100" alt="" />
                              <button type="button" class="absolute top-1.5 right-1.5 w-6 h-6 rounded-full bg-black/55 text-white border-none text-[0.7rem] cursor-pointer flex items-center justify-center leading-none hover:bg-red-600/90 transition-colors" @click="block.content = ''" title="Remove">✕</button>
                            </div>
                            <div v-else class="border-2 border-dashed rounded-xl p-5 flex items-center justify-center cursor-pointer transition select-none border-slate-200 bg-slate-50 hover:border-amber-400 hover:bg-amber-50" @click="uploadImageBlock(block)">
                              <div class="flex flex-col items-center gap-1 pointer-events-none">
                                <div class="text-3xl leading-none">🖼️</div>
                                <div class="text-sm font-semibold text-slate-600">Click to upload</div>
                                <div class="text-xs text-slate-400">or drag &amp; drop a file</div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </VueDraggable>
                      <div class="flex gap-2">
                        <button type="button" class="flex-1 py-2 border border-dashed border-slate-300 rounded-lg text-xs text-slate-400 hover:border-blue-400 hover:text-blue-500 transition cursor-pointer" @click="addTextBlock(form.explanationBlocks)">+ Add text</button>
                        <button type="button" class="flex-1 py-2 border border-dashed border-amber-200 rounded-lg text-xs text-amber-400 hover:border-amber-400 hover:text-amber-500 transition cursor-pointer" @click="addImageBlock(form.explanationBlocks)">+ Add image</button>
                      </div>
                    </div>

                    <!-- ── Unified Action Button (non-briefing) ── -->
                    <div
                      class="rounded-2xl border-2 p-4 space-y-3 transition-colors"
                      :class="form.showActionButton ? 'border-emerald-400 bg-emerald-50/60' : 'border-slate-200 bg-slate-50/70 opacity-60'"
                    >
                      <div class="flex items-center justify-between gap-3">
                        <div class="flex items-center gap-2">
                          <div class="w-7 h-7 rounded-lg flex items-center justify-center transition"
                            :class="form.showActionButton ? 'bg-emerald-500 text-white' : 'bg-slate-100 text-slate-400'">
                            <CheckCircle2 class="w-4 h-4" />
                          </div>
                          <div>
                            <h3 class="text-sm font-bold text-slate-900">Action Button</h3>
                            <p class="text-xs text-slate-500">Show a button after the reply so teams can proceed.</p>
                          </div>
                        </div>
                        <button type="button" role="switch" :aria-checked="form.showActionButton"
                          class="relative inline-flex h-5 w-9 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors focus:outline-none"
                          :class="form.showActionButton ? 'bg-emerald-500' : 'bg-slate-200'"
                          @click="form.showActionButton = !form.showActionButton">
                          <span class="pointer-events-none inline-block h-4 w-4 rounded-full bg-white shadow transform transition-transform"
                            :class="form.showActionButton ? 'translate-x-4' : 'translate-x-0'" />
                        </button>
                      </div>
                      <template v-if="form.showActionButton">
                        <div class="space-y-2">
                          <label class="block text-xs font-semibold tracking-wide text-slate-500 uppercase">Button Label</label>
                          <input v-model="form.actionButtonText" type="text" maxlength="255"
                            class="w-full px-3 py-2 text-sm border border-emerald-300 rounded-lg outline-none focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/20 transition bg-white text-slate-900 placeholder-slate-400"
                            placeholder="Next Question" />
                          <div class="flex justify-center pt-1">
                            <span class="px-5 py-1.5 rounded-full text-xs font-semibold bg-white border border-emerald-400 text-emerald-700 shadow-sm select-none">
                              {{ form.actionButtonText || 'Next Question' }}
                            </span>
                          </div>
                        </div>
                      </template>
                    </div>

                  </template>

                  <!-- ── Tab: Briefing ── -->
                  <template v-if="activeTab === 'briefing'">

                    <div class="space-y-1.5">
                      <label class="block text-sm font-semibold text-slate-700">
                        Briefing Content <span class="text-red-500">*</span>
                      </label>
                      <VueDraggable v-model="form.questionBlocks" :animation="150" handle=".drag-handle" class="space-y-2">
                        <div v-for="block in form.questionBlocks" :key="block._id" class="rounded-lg border border-slate-200 bg-white overflow-hidden">
                          <div class="flex items-center gap-1.5 px-2.5 py-1.5 bg-slate-50 border-b border-slate-100">
                            <div class="drag-handle cursor-grab text-slate-300 hover:text-slate-500 select-none text-base leading-none px-0.5" title="Drag to reorder">⠿</div>
                            <span class="text-xs font-semibold" :class="block.type === 'text' ? 'text-blue-600' : 'text-amber-600'">{{ block.type === 'text' ? 'Text' : 'Image' }}</span>
                            <div class="flex-1"></div>
                            <button type="button" class="text-slate-300 hover:text-red-500 transition text-xs leading-none cursor-pointer px-1" @click="removeBlock(form.questionBlocks, block._id)" title="Remove">✕</button>
                          </div>
                          <div v-if="block.type === 'text'" class="p-3 space-y-1.5">
                            <div class="flex items-center gap-0.5 px-1.5 py-1 bg-slate-50 border border-slate-200 rounded-lg w-fit">
                              <button type="button" class="px-1.5 py-0.5 text-xs font-bold text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<b>', '</b>')">B</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs italic text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<i>', '</i>')">I</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs underline text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<u>', '</u>')">U</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs line-through text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<s>', '</s>')">S</button>
                              <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                              <button type="button" class="px-1.5 py-0.5 text-xs font-mono text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, '<code>', '</code>')">&lt;/&gt;</button>
                              <button type="button" class="px-1.5 py-0.5 text-xs text-slate-600 hover:bg-slate-200 rounded transition cursor-pointer leading-none" title="Bullet" @click="insertBullet(questionTextareas[block._id] ?? null, form.questionBlocks, block._id)">•</button>
                              <div class="w-px h-3 bg-slate-200 mx-0.5"></div>
                              <button v-for="c in COLORS" :key="c.hex" type="button" :title="c.label" :style="{ backgroundColor: c.hex }" class="w-3.5 h-3.5 rounded-full cursor-pointer hover:scale-110 transition-transform flex-shrink-0" @click="applyBlockFormat(questionTextareas[block._id] ?? null, form.questionBlocks, block._id, colorTag(c.hex), '</span>')"></button>
                            </div>
                            <textarea
                              :ref="(el) => { questionTextareas[block._id] = el as HTMLTextAreaElement }"
                              v-model="block.content"
                              class="tg-auto-textarea w-full px-3 py-2 text-sm border border-slate-200 rounded-lg outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20 resize-none transition bg-white text-slate-900 placeholder-slate-400 font-[inherit]"
                              placeholder="Type here…"
                            ></textarea>
                          </div>
                          <div v-else class="p-3">
                            <div v-if="block.content" class="relative rounded-lg overflow-hidden">
                              <img :src="`/api/files/${block.content}`" class="max-h-48 w-full object-contain block rounded-lg bg-slate-100" alt="" />
                              <button type="button" class="absolute top-1.5 right-1.5 w-6 h-6 rounded-full bg-black/55 text-white border-none text-[0.7rem] cursor-pointer flex items-center justify-center leading-none hover:bg-red-600/90 transition-colors" @click="block.content = ''" title="Remove">✕</button>
                            </div>
                            <div v-else class="border-2 border-dashed rounded-xl p-5 flex items-center justify-center cursor-pointer transition select-none border-slate-200 bg-slate-50 hover:border-amber-400 hover:bg-amber-50" @click="uploadImageBlock(block)">
                              <div class="flex flex-col items-center gap-1 pointer-events-none">
                                <div class="text-3xl leading-none">🖼️</div>
                                <div class="text-sm font-semibold text-slate-600">Click to upload</div>
                                <div class="text-xs text-slate-400">or drag &amp; drop a file</div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </VueDraggable>
                      <div class="flex gap-2">
                        <button type="button" class="flex-1 py-2 border border-dashed border-slate-300 rounded-lg text-xs text-slate-400 hover:border-blue-400 hover:text-blue-500 transition cursor-pointer" @click="addTextBlock(form.questionBlocks)">+ Add text</button>
                        <button type="button" class="flex-1 py-2 border border-dashed border-amber-200 rounded-lg text-xs text-amber-400 hover:border-amber-400 hover:text-amber-500 transition cursor-pointer" @click="addImageBlock(form.questionBlocks)">+ Add image</button>
                      </div>
                    </div>

                    <!-- ── Unified Action Button (briefing) ── -->
                    <div
                      class="rounded-2xl border-2 p-4 space-y-3 transition-colors"
                      :class="form.showActionButton
                        ? (form.briefingButtonType === 'start-timer' ? 'border-amber-400 bg-amber-50/60' : 'border-emerald-400 bg-emerald-50/60')
                        : 'border-slate-200 bg-slate-50/70 opacity-60'"
                    >
                      <!-- Header row -->
                      <div class="flex items-center justify-between gap-3">
                        <div class="flex items-center gap-2">
                          <div class="w-7 h-7 rounded-lg flex items-center justify-center transition"
                            :class="form.showActionButton
                              ? (form.briefingButtonType === 'start-timer' ? 'bg-amber-500 text-white' : 'bg-emerald-500 text-white')
                              : 'bg-slate-100 text-slate-400'">
                            <Timer v-if="form.briefingButtonType === 'start-timer'" class="w-4 h-4" />
                            <CheckCircle2 v-else class="w-4 h-4" />
                          </div>
                          <div>
                            <h3 class="text-sm font-bold text-slate-900">Action Button</h3>
                            <p class="text-xs text-slate-500">Show a button on the briefing slide.</p>
                          </div>
                        </div>
                        <button type="button" role="switch" :aria-checked="form.showActionButton"
                          class="relative inline-flex h-5 w-9 shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors focus:outline-none"
                          :class="form.showActionButton
                            ? (form.briefingButtonType === 'start-timer' ? 'bg-amber-500' : 'bg-emerald-500')
                            : 'bg-slate-200'"
                          @click="form.showActionButton = !form.showActionButton">
                          <span class="pointer-events-none inline-block h-4 w-4 rounded-full bg-white shadow transform transition-transform"
                            :class="form.showActionButton ? 'translate-x-4' : 'translate-x-0'" />
                        </button>
                      </div>

                      <template v-if="form.showActionButton">
                        <!-- Button type selector -->
                        <div class="grid grid-cols-2 gap-2">
                          <button type="button"
                            class="flex items-center gap-2 px-3 py-2.5 rounded-xl border-2 transition cursor-pointer"
                            :class="form.briefingButtonType === 'advance'
                              ? 'border-emerald-400 bg-emerald-50 text-emerald-700'
                              : 'border-slate-200 bg-white text-slate-500 hover:border-emerald-200'"
                            @click="form.briefingButtonType = 'advance'; if (!form.actionButtonText || form.actionButtonText === 'Start Timer') form.actionButtonText = 'READY'">
                            <CheckCircle2 class="w-4 h-4 shrink-0" />
                            <div class="text-left">
                              <div class="text-xs font-semibold leading-tight">Advance</div>
                              <div class="text-[0.65rem] leading-tight opacity-70">Proceed to next</div>
                            </div>
                          </button>
                          <button type="button"
                            class="flex items-center gap-2 px-3 py-2.5 rounded-xl border-2 transition cursor-pointer"
                            :class="form.briefingButtonType === 'start-timer'
                              ? 'border-amber-400 bg-amber-50 text-amber-700'
                              : 'border-slate-200 bg-white text-slate-500 hover:border-amber-200'"
                            @click="form.briefingButtonType = 'start-timer'; if (!form.actionButtonText || form.actionButtonText === 'READY') form.actionButtonText = 'Start Timer'">
                            <Timer class="w-4 h-4 shrink-0" />
                            <div class="text-left">
                              <div class="text-xs font-semibold leading-tight">Start Timer</div>
                              <div class="text-[0.65rem] leading-tight opacity-70">Keeps slide visible</div>
                            </div>
                          </button>
                        </div>

                        <!-- Label + preview -->
                        <div class="space-y-2">
                          <label class="block text-xs font-semibold uppercase tracking-wide text-slate-500">Button Label</label>
                          <input v-model="form.actionButtonText" type="text" maxlength="255"
                            class="w-full px-3 py-2 text-sm border rounded-lg outline-none transition bg-white text-slate-900 placeholder-slate-400"
                            :class="form.briefingButtonType === 'start-timer'
                              ? 'border-amber-300 focus:border-amber-500 focus:ring-2 focus:ring-amber-500/20'
                              : 'border-emerald-300 focus:border-emerald-500 focus:ring-2 focus:ring-emerald-500/20'"
                            :placeholder="form.briefingButtonType === 'start-timer' ? 'Start Timer' : 'READY'" />
                          <div class="flex justify-center pt-1">
                            <span class="px-5 py-1.5 rounded-full text-xs font-semibold bg-white shadow-sm select-none"
                              :class="form.briefingButtonType === 'start-timer'
                                ? 'border border-amber-400 text-amber-700'
                                : 'border border-emerald-400 text-emerald-700'">
                              {{ form.actionButtonText || (form.briefingButtonType === 'start-timer' ? 'Start Timer' : 'READY') }}
                            </span>
                          </div>
                        </div>
                      </template>
                    </div>

                  </template>

                </form>
              </div>
            </div>

            <!-- Footer -->
            <div class="flex items-center justify-end gap-2 px-6 py-4 border-t border-slate-100 bg-slate-50/60 shrink-0">
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

          <!-- Preview panel (right side, desktop only) -->
          <div class="hidden md:flex w-full md:w-1/2 flex-col overflow-hidden border-l border-slate-100">

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
            <div class="flex-1 overflow-y-auto px-2 pt-2 pb-3 flex flex-col gap-1 tab-scroll" style="background: #e8edf2">

              <!-- Date chip -->
              <div class="flex justify-center mb-1.5">
                <span class="tg-date-chip">Today</span>
              </div>

                <!-- ── Briefing preview ── -->
                <template v-if="form.isBriefing">
                  <template v-for="block in form.questionBlocks" :key="'pb'+block._id">
                    <div v-if="block.type === 'text' && block.content.trim()" class="tg-row">
                      <div class="tg-avatar">🤖</div>
                      <div class="tg-msg"><span v-html="tgToHtml(block.content)"></span><span class="tg-ts">12:00</span></div>
                    </div>
                    <div v-else-if="block.type === 'image' && block.content" class="tg-row">
                      <div class="tg-avatar">🤖</div>
                      <div class="tg-msg tg-img-msg"><img :src="`/api/files/${block.content}`" class="tg-qimg" alt="" /></div>
                    </div>
                  </template>
                  <div v-if="!form.questionBlocks.some(b => b.content.trim())" class="tg-row">
                    <div class="tg-avatar">🤖</div>
                    <div class="tg-msg"><span class="text-slate-400 italic">Your instructions…</span><span class="tg-ts">12:00</span></div>
                  </div>
                  <div v-if="form.showActionButton" class="tg-keyboard">
                    <div v-if="form.briefingButtonType === 'start-timer'" class="tg-kb-btn">⏱ {{ form.actionButtonText || 'Start Timer' }}</div>
                    <div v-else class="tg-kb-btn tg-kb-ready">✅ {{ form.actionButtonText || 'READY' }}</div>
                  </div>
                </template>

                <!-- ── Question preview (Multiple Choice / Photo / Text Input) ── -->
                <template v-else>

                <template v-if="form.expectsTextInput">
                  <template v-if="form.questionBlocks.some(b => b.content.trim())">
                    <template v-for="block in form.questionBlocks" :key="'pt'+block._id">
                      <div v-if="block.type === 'text' && block.content.trim()" class="tg-row">
                        <div class="tg-avatar">🤖</div>
                        <div class="tg-msg"><span v-html="tgToHtml(block.content)"></span><span class="tg-ts">12:00</span></div>
                      </div>
                      <div v-else-if="block.type === 'image' && block.content" class="tg-row">
                        <div class="tg-avatar">🤖</div>
                        <div class="tg-msg tg-img-msg"><img :src="`/api/files/${block.content}`" class="tg-qimg" alt="" /></div>
                      </div>
                    </template>
                  </template>
                  <div v-else class="tg-row">
                    <div class="tg-avatar">🤖</div>
                    <div class="tg-msg"><span class="text-slate-400 italic">Ask the player to type something…</span><span class="tg-ts">12:00</span></div>
                  </div>
                  <div class="tg-row tg-row-user">
                    <div class="tg-msg tg-msg-user"><span class="text-slate-500 italic">&#123;&#123;Player types a reply here&#125;&#125;</span><span class="tg-ts">12:01</span></div>
                  </div>
                </template>

                <template v-else>

                <!-- Question blocks in order -->
                <template v-if="form.questionBlocks.some(b => b.content.trim())">
                  <template v-for="block in form.questionBlocks" :key="'pq'+block._id">
                    <div v-if="block.type === 'text' && block.content.trim()" class="tg-row">
                      <div class="tg-avatar">🤖</div>
                      <div v-if="block._id === lastFilledQTextId" class="tg-msg tg-poll">
                        <div class="tg-poll-q"><span v-html="tgToHtml(block.content)"></span><span class="tg-ts">12:00</span></div>
                      </div>
                      <div v-else class="tg-msg"><span v-html="tgToHtml(block.content)"></span><span class="tg-ts">12:00</span></div>
                    </div>
                    <div v-else-if="block.type === 'image' && block.content" class="tg-row">
                      <div class="tg-avatar">🤖</div>
                      <div class="tg-msg tg-img-msg"><img :src="`/api/files/${block.content}`" class="tg-qimg" alt="" /></div>
                    </div>
                  </template>
                </template>
                <template v-else>
                  <div class="tg-row">
                    <div class="tg-avatar">🤖</div>
                    <div class="tg-msg tg-poll">
                      <div class="tg-poll-q"><span class="text-slate-400 italic">Your question…</span><span class="tg-ts">12:00</span></div>
                    </div>
                  </div>
                </template>

                <!-- Inline keyboard quick replies -->
                <template v-if="!form.expectPhoto && filledOptions.length">
                  <div class="tg-keyboard">
                    <div v-for="(opt, i) in filledOptions" :key="'kb'+i" class="tg-kb-btn">
                      {{ String.fromCharCode(65 + i) }} · {{ opt }}
                    </div>
                    <div v-if="hasFilledHintBlocks" class="tg-kb-btn tg-kb-hint">💡 Hint</div>
                  </div>
                </template>

                <!-- After-hint service + hint blocks -->
                <template v-if="hasFilledHintBlocks">
                  <div class="flex justify-center my-1.5">
                    <span class="tg-service-msg">after hint button click</span>
                  </div>
                  <template v-for="block in form.hintBlocks" :key="'ph'+block._id">
                    <div v-if="block.type === 'text' && block.content.trim()" class="tg-row">
                      <div class="tg-avatar">🤖</div>
                      <div class="tg-msg tg-hint-msg">💡 <span v-html="tgToHtml(block.content)"></span><span class="tg-ts">12:01</span></div>
                    </div>
                    <div v-else-if="block.type === 'image' && block.content" class="tg-row">
                      <div class="tg-avatar">🤖</div>
                      <div class="tg-msg tg-img-msg"><img :src="`/api/files/${block.content}`" class="tg-qimg" alt="" /></div>
                    </div>
                  </template>
                </template>

                <!-- After-answer service message -->
                <div class="flex justify-center my-1.5">
                  <span class="tg-service-msg">after answer</span>
                </div>

                </template>

                </template> <!-- end v-else question preview -->

                <!-- After Answer blocks (not shown for briefing) -->
                <template v-if="!form.isBriefing">
                  <template v-if="hasFilledExplanationBlocks">
                    <template v-for="block in form.explanationBlocks" :key="'pe'+block._id">
                      <div v-if="block.type === 'text' && block.content.trim()" class="tg-row">
                        <div class="tg-avatar">🤖</div>
                        <div class="tg-msg tg-explanation-msg"><span v-html="tgToHtml(block.content)"></span><span class="tg-ts">12:02</span></div>
                      </div>
                      <div v-else-if="block.type === 'image' && block.content" class="tg-row">
                        <div class="tg-avatar">🤖</div>
                        <div class="tg-msg tg-img-msg"><img :src="`/api/files/${block.content}`" class="tg-qimg" alt="" /></div>
                      </div>
                    </template>
                  </template>
                  <div v-else-if="!form.showActionButton" class="tg-empty-explanation">No after answer content added yet…</div>
                  <div v-if="form.showActionButton" class="tg-keyboard">
                    <div class="tg-kb-btn tg-kb-ready">✅ {{ form.actionButtonText || 'Next Question' }}</div>
                  </div>
                </template>

            </div>

          </div>

        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup lang="ts">
import { ref, watch, computed, nextTick, onUnmounted } from 'vue'
import { ListChecks, Camera, BookOpen, Type, CheckCircle2, Timer } from '@lucide/vue'
import { VueDraggable } from 'vue-draggable-plus'
import type { Question, QuestionRequest, ContentBlock } from '@/types/question'
import type { Folder } from '@/types/folder'
import { questionsService } from '@/services/questionsService'

// ── FormBlock (internal form type with stable drag key) ──────────────────
interface FormBlock {
  _id: string
  type: 'text' | 'image'
  content: string
}

function makeId(): string {
  return Math.random().toString(36).slice(2) + Date.now().toString(36)
}

function makeBlock(type: 'text' | 'image', content = ''): FormBlock {
  return { _id: makeId(), type, content }
}

// ── Props / emits ──────────────────────────────────────────────────────────
const props = defineProps<{
  visible: boolean
  question: Question | null
  folders: Folder[]
  defaultFolderId?: number | null
}>()

const emit = defineEmits<{
  (e: 'close'): void
  (e: 'saved', q: Question): void
}>()

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Escape' && props.visible) {
    emit('close')
  }
}

watch(() => props.visible, (val) => {
  if (val) {
    document.addEventListener('keydown', onKeydown)
  } else {
    document.removeEventListener('keydown', onKeydown)
  }
})

onUnmounted(() => {
  document.removeEventListener('keydown', onKeydown)
})



// ── Form state ─────────────────────────────────────────────────────────────
function blankForm() {
  return {
    questionBlocks: [makeBlock('text')] as FormBlock[],
    options: [] as string[],
    answer: '',
    mark: null as number | null,
    expectPhoto: false,
    isBriefing: false,
    expectsTextInput: false,
    briefingPrimaryButtonText: 'READY',
    showBriefingPrimaryButton: false,
    briefingSecondaryButtonText: 'Start Timer',
    showBriefingSecondaryButton: false,
    briefingButtonType: 'advance' as 'advance' | 'start-timer',
    showActionButton: false,
    actionButtonText: 'READY',
    afterAnswerButtonText: 'Next Question',
    showAfterAnswerButton: false,
    hintBlocks: [] as FormBlock[],
    explanationBlocks: [] as FormBlock[],
    folderId: null as number | null,
  }
}

const form = ref(blankForm())
const saving = ref(false)
const error = ref('')
const isEdit = ref(false)
const activeTab = ref<'question' | 'answer' | 'mark' | 'hint' | 'explanation' | 'briefing'>('question')
const tabContentRef = ref<HTMLDivElement | null>(null)

watch(activeTab, () => {
  nextTick(() => {
    if (tabContentRef.value) {
      tabContentRef.value.scrollTop = 0
    }
  })
})
const tabs = computed(() => {
  if (form.value.isBriefing) {
    return [{ value: 'briefing' as const, label: 'Briefing' }]
  }
  if (form.value.expectsTextInput) {
    return [
      { value: 'question' as const, label: 'Question' },
      { value: 'mark' as const, label: 'Points' },
      { value: 'hint' as const, label: 'Hint' },
      { value: 'explanation' as const, label: 'After Answer' },
    ]
  }
  if (form.value.expectPhoto) {
    return [
      { value: 'question' as const, label: 'Question' },
      { value: 'mark' as const, label: 'Points' },
      { value: 'hint' as const, label: 'Hint' },
      { value: 'explanation' as const, label: 'After Answer' },
    ]
  }
  return [
    { value: 'question' as const, label: 'Question' },
    { value: 'answer' as const, label: 'Answer' },
    { value: 'mark' as const, label: 'Points' },
    { value: 'hint' as const, label: 'Hint' },
    { value: 'explanation' as const, label: 'After Answer' },
  ]
})
const filledOptions = computed(() => form.value.options.filter(o => o.trim()))
const hasFilledHintBlocks = computed(() => form.value.hintBlocks.some(b => b.content.trim()))
const hasFilledExplanationBlocks = computed(() => form.value.explanationBlocks.some(b => b.content.trim()))
const briefingPrimaryButtonLabel = computed(() => normalizeBriefingButtonText(form.value.briefingPrimaryButtonText, 'READY'))
const briefingSecondaryButtonLabel = computed(() => normalizeBriefingButtonText(form.value.briefingSecondaryButtonText, 'Start Timer'))
const showAnyBriefingButton = computed(() => form.value.showBriefingPrimaryButton || form.value.showBriefingSecondaryButton)
const afterAnswerButtonLabel = computed(() => normalizeBriefingButtonText(form.value.afterAnswerButtonText, 'Next Question'))
const lastFilledQTextId = computed(() => {
  const filled = form.value.questionBlocks.filter(b => b.type === 'text' && b.content.trim())
  return filled.length ? filled[filled.length - 1]._id : null
})

function normalizeBriefingButtonText(value: string | null | undefined, fallback: string): string {
  if (!value || !value.trim()) {
    return fallback
  }
  return value.trim()
}

// ── Rich text formatting ───────────────────────────────────────────────────
const questionTextareas = ref<Record<string, HTMLTextAreaElement | null>>({})
const hintTextareas = ref<Record<string, HTMLTextAreaElement | null>>({})
const explanationTextareas = ref<Record<string, HTMLTextAreaElement | null>>({})

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
    if (text.slice(end, end + close.length) === close) {
      const before = text.slice(0, start)
      const match = before.match(/<span style="color: [^"]+">$/)
      if (match) {
        const existingOpen = match[0]
        const preLen = existingOpen.length
        if (existingOpen === open) {
          const newText = text.slice(0, start - preLen) + text.slice(start, end) + text.slice(end + close.length)
          return { text: newText, selStart: start - preLen, selEnd: end - preLen }
        } else {
          const newText = text.slice(0, start - preLen) + open + text.slice(start, end) + close + text.slice(end + close.length)
          return { text: newText, selStart: start - preLen + open.length, selEnd: end - preLen + open.length }
        }
      }
    }
    const newText = text.slice(0, start) + open + text.slice(start, end) + close + text.slice(end)
    return { text: newText, selStart: start + open.length, selEnd: end + open.length }
  }
  if (start >= open.length && text.slice(start - open.length, start) === open && text.slice(end, end + close.length) === close) {
    const newText = text.slice(0, start - open.length) + text.slice(start, end) + text.slice(end + close.length)
    return { text: newText, selStart: start - open.length, selEnd: end - open.length }
  }
  const newText = text.slice(0, start) + open + text.slice(start, end) + close + text.slice(end)
  return { text: newText, selStart: start + open.length, selEnd: end + open.length }
}

function applyBlockFormat(
  el: HTMLTextAreaElement | null,
  blocks: FormBlock[],
  blockId: string,
  open: string,
  close: string
) {
  if (!el) return
  const block = blocks.find(b => b._id === blockId)
  if (!block) return
  const start = el.selectionStart ?? 0
  const end = el.selectionEnd ?? 0
  const result = toggleFormat(block.content, start, end, open, close)
  block.content = result.text
  nextTick(() => { el.focus(); el.setSelectionRange(result.selStart, result.selEnd) })
}

function insertBullet(
  el: HTMLTextAreaElement | null,
  blocks: FormBlock[],
  blockId: string
) {
  if (!el) return
  const block = blocks.find(b => b._id === blockId)
  if (!block) return
  const pos = el.selectionStart ?? block.content.length
  const bullet = '• '
  block.content = block.content.slice(0, pos) + bullet + block.content.slice(pos)
  nextTick(() => { el.focus(); el.setSelectionRange(pos + bullet.length, pos + bullet.length) })
}

function tgToHtml(text: string): string {
  if (!text) return ''

  const escaped = text
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')

  return escaped
    .replace(/&lt;br\s*\/??&gt;/gi, '<br>')
    .replace(/&lt;b&gt;(.*?)&lt;\/b&gt;/gis, '<strong>$1</strong>')
    .replace(/&lt;i&gt;(.*?)&lt;\/i&gt;/gis, '<em>$1</em>')
    .replace(/&lt;u&gt;(.*?)&lt;\/u&gt;/gis, '<u>$1</u>')
    .replace(/&lt;s&gt;(.*?)&lt;\/s&gt;/gis, '<s>$1</s>')
    .replace(/&lt;code&gt;(.*?)&lt;\/code&gt;/gis, '<code>$1</code>')
    .replace(
      /&lt;span style=&quot;color:\s*([^&]+?)&quot;&gt;(.*?)&lt;\/span&gt;/gis,
      '<span style="color: $1">$2</span>'
    )
    .replace(/\n/g, '<br>')
}

function setQuestionType(expectPhoto: boolean, isBriefing: boolean, expectsTextInput = false) {
  form.value.expectPhoto = expectPhoto
  form.value.isBriefing = isBriefing
  form.value.expectsTextInput = expectsTextInput
  activeTab.value = isBriefing ? 'briefing' : 'question'
  // Reset actionButtonText to the sensible default for the new type
  if (isBriefing) {
    if (!form.value.actionButtonText || form.value.actionButtonText === 'Next Question') {
      form.value.actionButtonText = form.value.briefingButtonType === 'start-timer' ? 'Start Timer' : 'READY'
    }
  } else {
    if (!form.value.actionButtonText || form.value.actionButtonText === 'READY' || form.value.actionButtonText === 'Start Timer') {
      form.value.actionButtonText = 'Next Question'
    }
  }
}

function onMarkInput(event: Event) {
  const value = (event.target as HTMLInputElement).value
  form.value.mark = value === '' ? null : Number(value)
}

watch(() => props.visible, (v) => {
  if (!v) return
  error.value = ''
  if (props.question) {
    const q = props.question
    activeTab.value = q.isBriefing ? 'briefing' : 'question'
    isEdit.value = true
    form.value = {
      questionBlocks: q.questionBlocks.length
        ? q.questionBlocks.map(b => ({ ...b, _id: makeId() }))
        : [makeBlock('text')],
      options: [...q.options],
      answer: q.answer ?? '',
      mark: q.mark ?? null,
      expectPhoto: q.expectPhoto,
      isBriefing: q.isBriefing,
      expectsTextInput: q.expectsTextInput ?? false,
      briefingPrimaryButtonText: q.briefingPrimaryButtonText ?? 'READY',
      showBriefingPrimaryButton: q.showBriefingPrimaryButton ?? false,
      briefingSecondaryButtonText: q.briefingSecondaryButtonText ?? 'Start Timer',
      showBriefingSecondaryButton: q.showBriefingSecondaryButton ?? false,
      briefingButtonType: (q.showBriefingSecondaryButton ? 'start-timer' : 'advance') as 'advance' | 'start-timer',
      showActionButton: q.isBriefing
        ? ((q.showBriefingPrimaryButton ?? false) || (q.showBriefingSecondaryButton ?? false))
        : (q.showAfterAnswerButton ?? false),
      actionButtonText: q.isBriefing
        ? (q.showBriefingSecondaryButton ? (q.briefingSecondaryButtonText ?? 'Start Timer') : (q.briefingPrimaryButtonText ?? 'READY'))
        : (q.afterAnswerButtonText ?? 'Next Question'),
      afterAnswerButtonText: q.afterAnswerButtonText ?? 'Next Question',
      showAfterAnswerButton: q.showAfterAnswerButton ?? false,
      hintBlocks: q.hintBlocks.map(b => ({ ...b, _id: makeId() })),
      explanationBlocks: q.explanationBlocks.map(b => ({ ...b, _id: makeId() })),
      folderId: q.folderId ?? null,
    }
    if (q.isBriefing) {
      activeTab.value = 'briefing'
    } else {
      activeTab.value = 'question'
    }
  } else {
    isEdit.value = false
    activeTab.value = 'question'
    form.value = { ...blankForm(), folderId: props.defaultFolderId ?? null }
  }
})

// ── Block helpers ──────────────────────────────────────────────────────────
function addTextBlock(list: FormBlock[]) { list.push(makeBlock('text')) }
function addImageBlock(list: FormBlock[]) { list.push(makeBlock('image')) }
function removeBlock(list: FormBlock[], id: string) {
  const idx = list.findIndex(b => b._id === id)
  if (idx !== -1) list.splice(idx, 1)
}

async function uploadImageBlock(block: FormBlock) {
  await new Promise<void>((resolve) => {
    const input = document.createElement('input')
    input.type = 'file'
    input.accept = 'image/*'
    input.onchange = async () => {
      const file = input.files?.[0]
      if (file) {
        try {
          block.content = await questionsService.uploadFile(file)
        } catch {
          error.value = 'Image upload failed.'
        }
      }
      resolve()
    }
    input.click()
  })
}

// ── Options helpers ────────────────────────────────────────────────────────
function addOption() { form.value.options.push('') }
function removeOption(i: number) {
  const removed = form.value.options.splice(i, 1)[0]
  if (form.value.answer === removed) form.value.answer = ''
}

async function submit() {
  if (!form.value.questionBlocks.some(b => b.type === 'text' && b.content.trim())) {
    error.value = 'At least one text block is required.'
    return
  }
  error.value = ''
  saving.value = true

  function toContentBlocks(list: FormBlock[]): ContentBlock[] {
    return list
      .filter(b => b.type === 'image' ? b.content.trim() : b.content.trim())
      .map(({ type, content }) => ({ type, content }))
  }

  const payload: QuestionRequest = {
    questionBlocks: toContentBlocks(form.value.questionBlocks),
    options: (form.value.expectPhoto || form.value.isBriefing || form.value.expectsTextInput) ? [] : form.value.options.filter(o => o.trim()),
    answer: (form.value.isBriefing || form.value.expectsTextInput) ? null : (form.value.answer || null),
    mark: form.value.isBriefing ? null : (form.value.mark ?? 0),
    expectPhoto: form.value.expectPhoto,
    isBriefing: form.value.isBriefing,
    expectsTextInput: form.value.expectsTextInput,
    briefingPrimaryButtonText: form.value.isBriefing
      ? (form.value.actionButtonText?.trim() || 'READY')
      : undefined,
    showBriefingPrimaryButton: form.value.isBriefing
      ? (form.value.showActionButton && form.value.briefingButtonType === 'advance')
      : undefined,
    briefingSecondaryButtonText: form.value.isBriefing
      ? (form.value.actionButtonText?.trim() || 'Start Timer')
      : undefined,
    showBriefingSecondaryButton: form.value.isBriefing
      ? (form.value.showActionButton && form.value.briefingButtonType === 'start-timer')
      : undefined,
    afterAnswerButtonText: !form.value.isBriefing
      ? (form.value.actionButtonText?.trim() || 'Next Question')
      : undefined,
    showAfterAnswerButton: !form.value.isBriefing ? form.value.showActionButton : undefined,
    hintBlocks: toContentBlocks(form.value.hintBlocks),
    explanationBlocks: toContentBlocks(form.value.explanationBlocks),
    folderId: form.value.folderId ?? null,
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
/* ── Auto-resizing textarea ── */
.tg-auto-textarea {
  field-sizing: content;
  min-height: 96px;
}

/* ── Scrollbar: always visible when content overflows, no layout shift ── */
.tab-scroll {
  scrollbar-gutter: stable;
  scrollbar-width: thin;
  scrollbar-color: rgba(0, 0, 0, 0.25) rgba(0, 0, 0, 0.08);
}
.tab-scroll::-webkit-scrollbar {
  width: 8px;
}
.tab-scroll::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.08);
  border-radius: 4px;
}
.tab-scroll::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.25);
  border-radius: 4px;
}
.tab-scroll::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.35);
}

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
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  font-weight: 400;
  color: #111;
  box-shadow: 0 1px 2px rgba(0,0,0,0.08);
  word-break: break-word;
  line-height: 1.45;
  position: relative;
  max-width: 82%;
  min-width: 0;
  overflow: hidden;
}

.tg-row-user {
  justify-content: flex-end;
}

.tg-msg-user {
  background: #e7ffdb;
  border-radius: 12px 2px 12px 12px;
}

.tg-msg :deep(b),
.tg-msg :deep(strong),
.tg-poll-q :deep(b),
.tg-poll-q :deep(strong) {
  font-weight: 700;
}

.tg-msg :deep(i),
.tg-msg :deep(em),
.tg-poll-q :deep(i),
.tg-poll-q :deep(em) {
  font-style: italic;
}

.tg-msg :deep(u),
.tg-poll-q :deep(u) {
  text-decoration: underline;
}

.tg-msg :deep(s),
.tg-msg :deep(del),
.tg-poll-q :deep(s),
.tg-poll-q :deep(del) {
  text-decoration: line-through;
}

.tg-msg :deep(code),
.tg-poll-q :deep(code) {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, 'Liberation Mono', monospace;
  font-size: 0.92em;
  background: rgba(15, 23, 42, 0.06);
  border-radius: 4px;
  padding: 0.05em 0.28em;
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

.tg-poll {
  padding: 0.4rem 0.55rem 0.3rem;
  max-width: 82%;
  font-size: 0.8rem;
}

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
  font-size: 0.8rem;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  font-weight: normal;
  color: #111;
  line-height: 1.45;
}

.tg-photo-note {
  font-size: 0.8rem;
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
  font-size: 0.8rem;
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
.tg-opt-text { flex: 1; color: #111; font-size: 0.8rem; }
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
  font-size: 0.8rem;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
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
  font-size: 0.8rem;
  color: #9ca3af;
  font-style: italic;
  text-align: center;
  padding: 0.5rem 0;
}
</style>
