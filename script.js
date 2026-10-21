// REPORT LINKS: the five Project files links open the team Google Docs.
// For assessment submission, add links to final PDF snapshots alongside them.
// Keep these live document links when updating the reports.
const gameJar = null; // e.g. 'files/game.jar', after uploading a tested build
// Add actual files before listing them. Example:
// {title:'Week 3 plan (PDF)', url:'progress/week-3.pdf'}
const snapshots = [];
const evidence = [];
function renderResources(id, items) {
  if (!items.length) return;
  const container = document.getElementById(id), ul = document.createElement('ul');
  items.forEach(item => { const li = document.createElement('li'), a = document.createElement('a'); a.href = item.url; a.textContent = item.title; li.append(a); ul.append(li) });
  container.replaceChildren(ul);
}
renderResources('snapshot-list', snapshots);
renderResources('evidence-list', evidence);
if (gameJar) { const a = document.createElement('a'); a.href = gameJar; a.textContent = 'Download game (JAR)'; a.setAttribute('download', ''); document.getElementById('game-download').replaceChildren(a) }
// Letters hop on pointer hover; keyboard focus animates the link as a wave.
// The unsplit words remain accessible names. Essential links work without JS.
const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)');
function jump(letter, delay = 0) {
  if (reducedMotion.matches || letter.classList.contains('bounce')) return;
  letter.style.setProperty('--delay', `${delay}ms`); letter.classList.add('bounce');
}
document.querySelectorAll('[data-jump]').forEach(element => {
  const text = element.textContent;
  if (element.getAttribute('aria-hidden') !== 'true') element.setAttribute('aria-label', text);
  element.textContent = '';
  Array.from(text).forEach((character, index) => {
    const letter = document.createElement('span'); letter.className = 'letter'; letter.setAttribute('aria-hidden', 'true');
    letter.textContent = character === ' ' ? '\u00a0' : character;
    letter.style.setProperty('--tilt', index % 2 ? '7deg' : '-7deg');
    letter.addEventListener('pointerenter', () => jump(letter));
    letter.addEventListener('animationend', () => letter.classList.remove('bounce'));
    element.append(letter);
  });
  element.addEventListener('focus', () => element.querySelectorAll('.letter').forEach((letter, index) => jump(letter, index * 22)));
  element.addEventListener('pointerdown', event => { if (event.pointerType === 'touch') element.querySelectorAll('.letter').forEach((letter, index) => jump(letter, index * 22)) });
});
reducedMotion.addEventListener('change', () => document.querySelectorAll('.bounce').forEach(letter => letter.classList.remove('bounce')));
