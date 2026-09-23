(() => {
    'use strict';
    const API = '/pet-projects/naruto/api';
    const $ = id => document.getElementById(id);
    let catalog = [], state = null, season = 1, chartDays = 30;
    let saving = false, loading = false, epoch = 0, toastTimer, dateIsAutomatic = true;
    let revealedDate = null;
    const openGroups = new Set();
    const initializedSeasons = new Set();
    const number = (value, digits = 1) => new Intl.NumberFormat('ru-RU', { maximumFractionDigits: digits }).format(value);
    const date = (value, options = { day: 'numeric', month: 'short', year: 'numeric' }) => value
        ? new Intl.DateTimeFormat('ru-RU', { ...options, timeZone: 'UTC' }).format(new Date(value + 'T12:00:00Z')) : '—';
    const text = (id, value) => { $(id).textContent = value; };
    const allItems = () => catalog.flatMap(group => group.items);
    const nextItem = () => allItems().find(item => !state.marks[item.id]);

    function toast(message, error = false) {
        clearTimeout(toastTimer);
        text('toast', message);
        $('toast').classList.toggle('error', error);
        $('toast').hidden = false;
        toastTimer = setTimeout(() => { $('toast').hidden = true; }, error ? 7000 : 3500);
    }

    async function request(path, options = {}) {
        const controller = new AbortController();
        const timeout = setTimeout(() => controller.abort(), 15000);
        try {
            const response = await fetch(API + path, {
                ...options,
                credentials: 'same-origin',
                cache: 'no-store',
                signal: controller.signal,
                headers: {
                    'Accept': 'application/json',
                    ...(options.body ? { 'Content-Type': 'application/json' } : {}),
                    ...(options.headers || {})
                }
            });
            if (response.redirected || response.status === 401 || response.status === 403) {
                window.location.assign('/pet-projects/naruto');
                throw new Error('Сессия завершена. Войди заново.');
            }
            if (!response.ok) {
                const error = new Error(response.status === 409
                    ? 'Друг уже изменил эту отметку. Обновляем журнал.'
                    : response.status === 400 ? 'Проверь дату просмотра: она не может быть в будущем.'
                    : 'Не удалось связаться с сервером. Попробуй ещё раз.');
                error.status = response.status;
                throw error;
            }
            return response.json();
        } finally {
            clearTimeout(timeout);
        }
    }

    function updateDashboard() {
        const s = state.stats;
        text('pace-value', number(s.pace));
        const difference = s.pace - 3;
        text('pace-trend', s.status === 'NOT_STARTED' ? 'Ориентир — 3 в день'
            : Math.abs(difference) < .01 ? '→ В темпе 3 в день'
            : `${difference > 0 ? '↗' : '↘'} ${number(Math.abs(difference))} ${difference > 0 ? 'выше' : 'ниже'} цели`);
        $('pace-trend').classList.toggle('slow', difference < 0);
        text('today-count', s.todayCount);
        text('week-count', s.weekCount);
        text('pace-window', s.status === 'NOT_STARTED' || (s.today === s.start && !s.todayCount) ? 'Стартовый ориентир: 3 в день'
            : `Среднее за ${s.paceWindowDays} календ. дн.`);
        text('progress-percent', number(s.percent));
        $('total-progress').value = s.percent;
        text('watched-count', s.watched);
        text('total-count', s.total);
        text('remaining-count', s.remaining);
        for (const n of [1, 2]) {
            const items = allItems().filter(item => item.season === n);
            const count = items.filter(item => state.marks[item.id]).length;
            const prefix = n === 1 ? 'season-one' : 'season-two';
            text(prefix + '-count', `${count} / ${items.length}`);
            $(prefix + '-progress').max = items.length;
            $(prefix + '-progress').value = count;
            text('tab-count-' + n, `${count}/${items.length}`);
        }
        const missions = {
            NOT_STARTED: ['Скоро отправляемся', 'Старт 24 сентября 2026. Можно отмечать уже просмотренное.'],
            ON_TRACK: ['Успеваем к дедлайну', 'Текущий темп позволяет завершить маршрут за год.'],
            AT_RISK: ['Нужно набрать темп', 'При текущей частоте финиш будет после дедлайна.'],
            PAUSED: ['Пора на следующую серию', 'За расчётный период нет просмотров. Прогноз на паузе.'],
            WON: ['Миссия выполнена!', 'Весь маршрут пройден до дедлайна. Вы победили.'],
            LOST: ['Миссия проиграна', 'За 12 месяцев не успели. Журнал остаётся доступен.']
        };
        const mission = missions[s.status];
        text('mission-heading', mission[0]);
        text('mission-detail', mission[1]);
        document.querySelector('.mission-panel').dataset.status = s.status;
        text('forecast-date', s.predictedFinish ? date(s.predictedFinish) : 'Пока нет даты');
        text('forecast-hint', !s.remaining ? 'Фактическое завершение' : s.predictedFinish
            ? `Если держать ${number(s.pace)} в день` : 'Возобновите просмотр для прогноза');
        text('deadline-days', `${s.daysLeft} дн.`);
        text('required-pace', !s.remaining ? 'Все пункты отмечены'
            : s.status === 'LOST' ? 'Срок миссии истёк' : `Нужно ≥ ${number(Math.ceil(s.requiredPace * 100) / 100, 2)} в день`);
        text('baseline-finish', `По исходному плану 3 в день финиш — ${date(s.baselineFinish)}.`);
        const next = nextItem();
        text('next-caption', next ? `Далее: сезон ${next.season} · ${next.kind === 'EPISODE' ? 'серия ' : ''}${next.label}` : 'Все пункты маршрута просмотрены.');
        $('go-next').disabled = !next || saving;
        $('reveal-character').disabled = false;
        $('watch-date').max = s.today;
        if (dateIsAutomatic || !$('watch-date').value) $('watch-date').value = s.today;
        drawChart();
        updateCharacter();
    }

    function drawChart() {
        const series = state.stats.activity.slice(-chartDays);
        const svg = $('pace-chart');
        svg.replaceChildren();
        const ns = 'http://www.w3.org/2000/svg';
        function element(name, attrs = {}, content = null, parent = svg) {
            const node = document.createElementNS(ns, name);
            Object.entries(attrs).forEach(([key, value]) => node.setAttribute(key, value));
            if (content !== null) node.textContent = content;
            parent.append(node);
            return node;
        }
        element('title', { id: 'chart-title' }, 'Просмотры за последние ' + chartDays + ' дней');
        element('desc', { id: 'chart-description' }, 'Голубая линия — количество просмотров за день. Оранжевый пунктир — цель 3 в день. Дни без просмотра показаны нулём. Точные значения доступны в таблице под графиком.');
        const maximum = Math.max(4, ...series.map(day => day.count));
        const width = Math.max(280, svg.clientWidth || 700);
        const right = width - 14;
        svg.setAttribute('viewBox', `0 0 ${width} 200`);
        const x = i => 34 + (i / (series.length - 1)) * (right - 34);
        const y = count => 166 - (count / maximum) * 143;
        const defs = element('defs');
        const gradient = element('linearGradient', { id: 'pace-fill', x1: '0', y1: '0', x2: '0', y2: '1' }, null, defs);
        element('stop', { offset: '0%', 'stop-color': '#60b8f8', 'stop-opacity': '.22' }, null, gradient);
        element('stop', { offset: '100%', 'stop-color': '#60b8f8', 'stop-opacity': '0' }, null, gradient);
        for (const value of [...new Set([0, Math.round(maximum / 2), maximum])]) {
            element('line', { x1: 34, y1: y(value), x2: right, y2: y(value), stroke: '#263c51', 'stroke-width': .8 });
            element('text', { x: 21, y: y(value) + 4, fill: '#91a9c1', 'font-size': 10, 'text-anchor': 'end' }, value);
        }
        element('line', { x1: 34, y1: y(3), x2: right, y2: y(3), stroke: '#ff943d', 'stroke-width': 1.3, 'stroke-dasharray': '5 5', opacity: .7 });
        const points = series.map((day, i) => `${x(i)},${y(day.count)}`);
        element('path', { d: `M${x(0)},166 L${points.join(' L')} L${x(series.length - 1)},166 Z`, fill: 'url(#pace-fill)' });
        element('polyline', { points: points.join(' '), fill: 'none', stroke: '#60b8f8', 'stroke-width': 2.5, 'stroke-linejoin': 'round', 'stroke-linecap': 'round' });
        series.forEach((day, i) => {
            const circle = element('circle', { cx: x(i), cy: y(day.count), r: day.count ? 4 : 2.2, fill: day.count ? '#9bd6ff' : '#39739f' });
            element('title', {}, `${date(day.date)}: ${day.count}`, circle);
        });
        for (const index of [...new Set([0, Math.round((series.length - 1) / 3), Math.round((series.length - 1) * 2 / 3), series.length - 1])]) {
            element('text', { x: x(index), y: 190, fill: '#91a9c1', 'font-size': 10, 'text-anchor': index === 0 ? 'start' : index === series.length - 1 ? 'end' : 'middle' }, date(series[index].date, { day: 'numeric', month: 'short' }));
        }
        const body = $('chart-table');
        body.replaceChildren();
        series.forEach(day => {
            const row = document.createElement('tr');
            for (const value of [date(day.date), day.count]) {
                const cell = document.createElement('td'); cell.textContent = value; row.append(cell);
            }
            body.append(row);
        });
    }

    function updateCharacter() {
        const character = state.character;
        const key = `naruto-character:${state.username}:${character.date}`;
        let revealed = revealedDate === character.date;
        try { revealed ||= localStorage.getItem(key) === 'revealed'; } catch (_) { /* Private mode can disable storage. */ }
        $('character-result').hidden = !revealed;
        $('reveal-character').hidden = revealed;
        text('character-name', character.name);
        $('character-link').href = character.url;
        text('character-hint', revealed ? `Твой персонаж на ${date(character.date, { day: 'numeric', month: 'long' })}. Завтра — новый.`
            : `${character.poolSize} персонажей. Один выбор на день, свой для каждого.`);
    }

    function itemMatches(item, query) {
        if (!query) return true;
        if (/^\d+$/.test(query)) return item.number === Number(query);
        const label = item.kind === 'EPISODE' ? `серия ${item.number}` : item.label;
        return label.toLocaleLowerCase('ru').includes(query) || (query.includes('ова') && item.kind === 'OVA');
    }

    function renderGuide() {
        if (!state) return;
        const query = $('episode-search').value.trim().toLocaleLowerCase('ru');
        const hideWatched = $('only-unwatched').checked;
        const groups = catalog.filter(group => group.season === season);
        if (!initializedSeasons.has(season)) {
            const first = groups.find(group => group.items.some(item => !state.marks[item.id])) || groups[0];
            if (first) openGroups.add(first.id);
            initializedSeasons.add(season);
        }
        const container = $('episode-groups');
        container.replaceChildren();
        for (const group of groups) {
            const visibleItems = group.items.filter(item => itemMatches(item, query) && (!hideWatched || !state.marks[item.id]));
            if (!visibleItems.length) continue;
            const details = document.createElement('details');
            details.className = 'episode-group'; details.dataset.group = group.id;
            details.open = !!query || openGroups.has(group.id);
            const summary = document.createElement('summary');
            const title = document.createElement('span'); title.className = 'group-title'; title.textContent = group.title;
            const tag = document.createElement('span'); tag.className = 'group-tag' + (group.tags.startsWith('Ф') ? ' filler' : ''); tag.textContent = group.tags;
            const count = document.createElement('span'); count.className = 'group-count'; count.dataset.count = group.id;
            summary.append(title, tag, count);
            const grid = document.createElement('div'); grid.className = 'episode-grid';
            for (const item of visibleItems) {
                const button = document.createElement('button');
                button.type = 'button'; button.id = 'tile-' + item.id; button.dataset.item = item.id;
                button.className = 'episode-tile' + (item.kind !== 'EPISODE' ? ' special' : '');
                const check = document.createElement('span'); check.className = 'tile-check'; check.setAttribute('aria-hidden', 'true'); check.textContent = '✓';
                const label = document.createElement('span'); label.textContent = item.label;
                button.append(check, label); button.addEventListener('click', () => toggleItem(item)); grid.append(button);
            }
            details.append(summary, grid);
            details.addEventListener('toggle', () => {
                if (!details.isConnected || $('episode-search').value.trim()) return;
                if (details.open) openGroups.add(group.id); else openGroups.delete(group.id);
                updateExpandLabel();
            });
            container.append(details);
        }
        if (!container.children.length) {
            const empty = document.createElement('p'); empty.className = 'empty-guide';
            empty.textContent = query ? 'В вашем списке такой серии нет. Проверь номер или сезон.' : 'В этом сезоне всё просмотрено.';
            container.append(empty);
        }
        $('season-panel').setAttribute('aria-labelledby', 'season-tab-' + season);
        $('season-panel').setAttribute('aria-busy', String(saving));
        document.querySelectorAll('[data-season]').forEach(button => {
            const selected = Number(button.dataset.season) === season;
            button.setAttribute('aria-selected', String(selected)); button.tabIndex = selected ? 0 : -1;
        });
        $('watch-link').href = season === 1 ? 'https://jutsu.tv/181-naruto-k2.html' : 'https://jut-su.works/naruto-shippuuden';
        text('watch-link', `Смотреть ${season} сезон ↗`);
        updateTiles();
        updateExpandLabel();
    }

    function updateExpandLabel() {
        const details = [...document.querySelectorAll('.episode-group')];
        text('expand-groups', details.length && details.every(group => group.open) ? 'Свернуть все' : 'Развернуть все');
    }

    function updateTiles() {
        const next = nextItem();
        for (const item of allItems()) {
            const button = $('tile-' + item.id);
            if (!button) continue;
            const mark = state.marks[item.id];
            button.setAttribute('aria-pressed', String(!!mark));
            button.setAttribute('aria-label', `Сезон ${item.season}, ${item.kind === 'EPISODE' ? 'серия ' : ''}${item.label}, ${mark ? 'просмотрено' : 'не просмотрено'}`);
            button.title = mark ? `Просмотрено ${date(mark.watchedOn)} · отметил ${mark.markedBy}. Нажми, чтобы снять отметку.` : 'Отметить просмотр';
            button.querySelector('.tile-check').hidden = !mark;
            button.classList.toggle('next-up', next?.id === item.id);
            button.disabled = saving;
        }
        for (const group of catalog) {
            const count = document.querySelector(`[data-count="${group.id}"]`);
            if (!count) continue;
            const done = group.items.filter(item => state.marks[item.id]).length;
            count.textContent = `${done}/${group.items.length}`;
            count.closest('details').classList.toggle('finished', done === group.items.length);
        }
    }

    async function toggleItem(item) {
        if (saving || !state) return;
        const mark = state.marks[item.id];
        if (!mark && !$('watch-date').reportValidity()) return;
        saving = true;
        epoch++; // Ignore any older poll response while a save is in flight.
        text('sync-status', 'Сохраняем…');
        updateTiles();
        $('tile-' + item.id)?.classList.add('saving');
        $('season-panel').setAttribute('aria-busy', 'true');
        let refreshAfter = false;
        try {
            const header = document.querySelector('meta[name="_csrf_header"]').content;
            const token = document.querySelector('meta[name="_csrf"]').content;
            state = await request('/items/' + encodeURIComponent(item.id), {
                method: 'PUT', headers: { [header]: token },
                body: JSON.stringify({ watched: !mark, watchedOn: $('watch-date').value, expectedMarkedAt: mark?.markedAt || null })
            });
            updateDashboard();
            text('sync-status', 'Сохранено');
            toast(`${item.kind === 'EPISODE' ? 'Серия ' : ''}${item.label} · ${mark ? 'отметка снята' : 'просмотрено'}`);
        } catch (error) {
            refreshAfter = true;
            text('sync-status', 'Проверяем запись…');
            toast(error.status === 409 ? error.message : 'Не удалось подтвердить сохранение. Обновляем прогресс; проверь отметку.', true);
        } finally {
            saving = false;
            $('tile-' + item.id)?.classList.remove('saving');
            $('season-panel').setAttribute('aria-busy', 'false');
            if ($('only-unwatched').checked) renderGuide(); else updateTiles();
            $('go-next').disabled = !nextItem();
            const target = $('tile-' + item.id) || document.querySelector('.episode-tile:not([aria-pressed="true"])');
            if (target && !refreshAfter) target.focus({ preventScroll: true });
        }
        if (refreshAfter) await refresh();
    }

    async function refresh() {
        if (saving || loading) return;
        loading = true;
        const myEpoch = ++epoch;
        try {
            const latest = await request('/state');
            if (myEpoch !== epoch) return;
            const changed = !state || JSON.stringify(latest.marks) !== JSON.stringify(state.marks);
            state = latest;
            updateDashboard();
            if ($('only-unwatched').checked && changed) renderGuide(); else updateTiles();
            text('sync-status', 'Синхронизировано');
            $('load-error').hidden = true;
        } catch (error) {
            if (myEpoch !== epoch) return;
            text('sync-status', 'Нет связи');
            $('load-error').hidden = false;
            text('load-error-text', 'Связь потеряна. Показан последний загруженный прогресс.');
        } finally { loading = false; }
    }

    async function load() {
        if (loading) return;
        loading = true;
        text('sync-status', 'Подключаемся…');
        try {
            const [groups, initialState] = await Promise.all([request('/catalog'), request('/state')]);
            catalog = groups; state = initialState;
            season = nextItem()?.season || 1;
            updateDashboard(); renderGuide();
            text('sync-status', 'Синхронизировано');
            $('load-error').hidden = true;
        } catch (error) {
            $('load-error').hidden = false;
            text('load-error-text', 'Не удалось загрузить журнал. Проверь подключение и повтори.');
            text('sync-status', 'Нет связи');
        } finally { loading = false; }
    }

    document.querySelectorAll('[data-days]').forEach(button => button.addEventListener('click', () => {
        chartDays = Number(button.dataset.days);
        document.querySelectorAll('[data-days]').forEach(b => b.setAttribute('aria-pressed', String(b === button)));
        if (state) drawChart();
    }));
    document.querySelectorAll('[data-season]').forEach(button => {
        button.addEventListener('click', () => { season = Number(button.dataset.season); renderGuide(); });
        button.addEventListener('keydown', event => {
            if (['ArrowLeft', 'ArrowRight', 'Home', 'End'].includes(event.key)) {
                event.preventDefault(); season = event.key === 'Home' ? 1 : event.key === 'End' ? 2 : season === 1 ? 2 : 1;
                renderGuide(); $('season-tab-' + season).focus();
            }
        });
    });
    $('episode-search').addEventListener('input', renderGuide);
    $('only-unwatched').addEventListener('change', renderGuide);
    $('watch-date').addEventListener('change', () => { dateIsAutomatic = $('watch-date').value === state?.stats.today; });
    $('expand-groups').addEventListener('click', () => {
        const groups = [...document.querySelectorAll('.episode-group')];
        const expand = !groups.every(group => group.open);
        groups.forEach(group => { group.open = expand; if (expand) openGroups.add(group.dataset.group); else openGroups.delete(group.dataset.group); });
        updateExpandLabel();
    });
    $('go-next').addEventListener('click', () => {
        const next = nextItem(); if (!next) return;
        season = next.season; $('episode-search').value = ''; $('only-unwatched').checked = false;
        openGroups.add(catalog.find(group => group.items.some(item => item.id === next.id)).id);
        renderGuide();
        const tile = $('tile-' + next.id);
        tile.scrollIntoView({ block: 'center', behavior: matchMedia('(prefers-reduced-motion: reduce)').matches ? 'instant' : 'smooth' });
        tile.focus({ preventScroll: true });
    });
    $('reveal-character').addEventListener('click', () => {
        if (!state) return;
        revealedDate = state.character.date;
        try { localStorage.setItem(`naruto-character:${state.username}:${revealedDate}`, 'revealed'); } catch (_) { /* Session-only reveal still works. */ }
        document.querySelector('.daily-panel').classList.add('revealed');
        updateCharacter();
        $('character-link').focus({ preventScroll: true });
    });
    $('retry-load').addEventListener('click', () => state ? refresh() : load());
    document.addEventListener('visibilitychange', () => { if (!document.hidden && state) refresh(); });
    window.addEventListener('online', () => state ? refresh() : load());
    setInterval(() => { if (!document.hidden && state) refresh(); }, 20000);
    let resizeFrame;
    window.addEventListener('resize', () => {
        cancelAnimationFrame(resizeFrame);
        resizeFrame = requestAnimationFrame(() => { if (state) drawChart(); });
    });
    load();
})();
