// REPORT LINKS: the five Project files links open the team Google Docs.
// For assessment submission, add links to final PDF snapshots alongside them.
// Keep these live document links when updating the reports.
// Hosted URLs of the submitted PDFs (Table 1 filenames). A PDF link appears beside each Google Doc once set.
const pdfLinks = {
  Req1: '',
  Arch1: '',
  Plan1: '',
  Risk1: '',
  Impl1: ''
};

document.querySelectorAll('[data-pdf]').forEach(a => {
  const url = pdfLinks[a.dataset.pdf];
  if (!url) return;
  a.href = url;
  a.textContent = `${a.dataset.pdf}.pdf ↗`;
  a.hidden = false;
});

// The JAR is built and published by .github/workflows/release-jar.yml; the link is set in index.html.
const gameJar = null;

// Add actual files before listing them. Example:
// {title:'Week 3 plan (PDF)', url:'progress/week-3.pdf'}
const snapshots = [];
const evidence = [];

function renderResources(id, items) {
  if (!items.length) return;

  const container = document.getElementById(id);
  const ul = document.createElement('ul');

  items.forEach(item => {
    const li = document.createElement('li');
    const a = document.createElement('a');

    a.href = item.url;
    a.textContent = item.title;
    li.append(a);
    ul.append(li);
  });

  container.replaceChildren(ul);
}

renderResources('snapshot-list', snapshots);
renderResources('evidence-list', evidence);

if (gameJar) {
  const a = document.createElement('a');

  a.href = gameJar;
  a.textContent = 'Download game (JAR)';
  a.setAttribute('download', '');
  document.getElementById('game-download').replaceChildren(a);
}

// Letters hop on pointer hover; keyboard focus animates the link as a wave.
// The unsplit words remain accessible names. Essential links work without JS.
const reducedMotion = window.matchMedia('(prefers-reduced-motion: reduce)');

function jump(letter, delay = 0) {
  if (reducedMotion.matches || letter.classList.contains('bounce')) return;

  letter.style.setProperty('--delay', `${delay}ms`);
  letter.classList.add('bounce');
}

function jumpAll(element) {
  element.querySelectorAll('.letter').forEach((letter, index) => jump(letter, index * 22));
}

document.querySelectorAll('[data-jump]').forEach(element => {
  const text = element.textContent;

  if (element.getAttribute('aria-hidden') !== 'true') {
    element.setAttribute('aria-label', text);
  }

  element.textContent = '';

  Array.from(text).forEach((character, index) => {
    const letter = document.createElement('span');

    letter.className = 'letter';
    letter.setAttribute('aria-hidden', 'true');
    letter.textContent = character === ' ' ? ' ' : character;
    letter.style.setProperty('--tilt', index % 2 ? '7deg' : '-7deg');

    letter.addEventListener('pointerenter', () => jump(letter));
    letter.addEventListener('animationend', () => letter.classList.remove('bounce'));

    element.append(letter);
  });

  element.addEventListener('focus', () => jumpAll(element));

  element.addEventListener('pointerdown', event => {
    if (event.pointerType === 'touch') jumpAll(element);
  });
});

reducedMotion.addEventListener('change', () => {
  document.querySelectorAll('.bounce').forEach(letter => letter.classList.remove('bounce'));
});

const ganttChart = document.querySelectorAll('.chart');
const modal = document.getElementById('photo-modal');
const modalImg = document.getElementById('modal-img');

ganttChart.forEach(photo => {
	photo.addEventListener('click', () => {
		modalImg.src = photo.src;
		modalImg.alt = photo.alt;
		modal.showModal();
	});
});

modal.addEventListener('click', () => {
	modal.close();
});