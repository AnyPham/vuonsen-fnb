import { Outlet } from 'react-router-dom';
import Header from './Header';
import Footer from './Footer';
import AssistantWidget from '@/components/common/AssistantWidget';

export default function Layout() {
  return (
    <>
      <Header />
      <main>
        <Outlet />
      </main>
      <Footer />
      <AssistantWidget />
    </>
  );
}
