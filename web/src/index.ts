const imgEl = document.getElementById('frame') as HTMLImageElement;
const fpsEl = document.getElementById('fps') as HTMLSpanElement;
const resEl = document.getElementById('res') as HTMLSpanElement;

const sample = 'data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAuMB9TQUPLkAAAAASUVORK5CYII=';

function loadSample() {
  imgEl.src = sample;
  imgEl.onload = () => {
    resEl.textContent = `${imgEl.naturalWidth}x${imgEl.naturalHeight}`;
    fpsEl.textContent = 'N/A';
  };
}

function simulateStatsUpdate() {
  let t = 0;
  setInterval(() => {
    const fps = 12 + Math.sin(t) * 3;
    fpsEl.textContent = fps.toFixed(1);
    t += 0.2;
  }, 500);
}

loadSample();
simulateStatsUpdate();


