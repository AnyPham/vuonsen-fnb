import { useEffect, useState } from 'react';
import { galleryApi } from '@/api/endpoints';
import { Empty, Loading } from '@/components/common/StateBlock';
import Thumb from '@/components/common/Thumb';
import { useI18n } from '@/i18n';

// Thư viện ảnh dạng lưới, bấm vào ảnh để xem lớn
export default function GalleryPage() {
  const [images, setImages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [current, setCurrent] = useState(null); // vị trí ảnh đang mở
  const { t, tDb } = useI18n();

  useEffect(() => {
    galleryApi
      .list()
      .then(setImages)
      .catch(() => setImages([]))
      .finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    if (current === null) return undefined;

    const onKey = (event) => {
      if (event.key === 'Escape') setCurrent(null);
      if (event.key === 'ArrowLeft') setCurrent((i) => (i - 1 + images.length) % images.length);
      if (event.key === 'ArrowRight') setCurrent((i) => (i + 1) % images.length);
    };
    document.addEventListener('keydown', onKey);
    document.body.style.overflow = 'hidden';
    return () => {
      document.removeEventListener('keydown', onKey);
      document.body.style.overflow = '';
    };
  }, [current, images.length]);

  return (
    <section className="section">
      <div className="wrap">
        <div className="section-head center">
          <div className="eyebrow center">{t('gallery.eyebrow')}</div>
          <h2>{t('gallery.title')}</h2>
          <p className="muted">{t('gallery.hint')}</p>
        </div>

        {loading && <Loading />}
        {!loading && images.length === 0 && <Empty label={t('gallery.empty')} />}

        <div className="grid grid-3">
          {images.map((image, index) => (
            <button
              key={image.id}
              type="button"
              className="card"
              style={{ padding: 0, border: 0, cursor: 'zoom-in' }}
              onClick={() => setCurrent(index)}
            >
              <Thumb
                url={image.url}
                variant={index % 3 === 1 ? 'v2' : index % 3 === 2 ? 'v3' : ''}
                icon="🖼️"
                label={tDb(image, 'caption')}
                alt={tDb(image, 'caption')}
              />
            </button>
          ))}
        </div>
      </div>

      {current !== null && images[current] && (
        <div
          role="dialog"
          aria-modal="true"
          aria-label={tDb(images[current], 'caption')}
          onClick={() => setCurrent(null)}
          style={{
            position: 'fixed',
            inset: 0,
            background: 'rgba(18,40,28,.94)',
            display: 'grid',
            placeItems: 'center',
            zIndex: 100,
            padding: 20,
          }}
        >
          <button
            type="button"
            className="btn btn-ghost"
            style={{ position: 'absolute', top: 16, right: 20, color: 'var(--cream)', fontSize: '1.6rem' }}
            onClick={() => setCurrent(null)}
            aria-label={t('gallery.close')}
          >
            ×
          </button>

          <div onClick={(e) => e.stopPropagation()} style={{ maxWidth: 900, width: '100%' }}>
            <Thumb
              url={images[current].url}
              variant="v3"
              icon="🖼️"
              label={tDb(images[current], 'caption')}
              alt={tDb(images[current], 'caption')}
              style={{ borderRadius: 'var(--r)' }}
            />
            <div style={{ display: 'flex', justifyContent: 'space-between', marginTop: 16 }}>
              <button
                type="button"
                className="btn btn-outline btn-sm"
                style={{ borderColor: 'var(--cream)', color: 'var(--cream)' }}
                onClick={() => setCurrent((i) => (i - 1 + images.length) % images.length)}
              >
                {t('gallery.prev')}
              </button>
              <span style={{ color: 'var(--cream)' }}>
                {current + 1} / {images.length}
              </span>
              <button
                type="button"
                className="btn btn-outline btn-sm"
                style={{ borderColor: 'var(--cream)', color: 'var(--cream)' }}
                onClick={() => setCurrent((i) => (i + 1) % images.length)}
              >
                {t('gallery.next')}
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}
