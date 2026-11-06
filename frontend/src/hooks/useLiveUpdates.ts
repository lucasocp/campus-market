import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';

/** WebSocket invalida consultas; reconexão e polling recuperam eventos perdidos. */
export function useLiveUpdates(enabled: boolean, refresh: () => void) {
  const callback = useRef(refresh);
  callback.current = refresh;
  useEffect(() => {
    if (!enabled) return;
    let timer: ReturnType<typeof setTimeout>;
    const update = () => { clearTimeout(timer); timer = setTimeout(() => callback.current(), 250); };
    const client = new Client({
      brokerURL: `${location.protocol === 'https:' ? 'wss' : 'ws'}://${location.host}/ws`,
      reconnectDelay: 5000,
      onConnect: () => {
        client.subscribe('/topic/catalog', update);
        client.subscribe('/user/queue/updates', update);
        update();
      },
    });
    client.activate();
    const fallback = setInterval(() => { if (!document.hidden) callback.current(); }, 20000);
    const onFocus = () => callback.current();
    window.addEventListener('focus', onFocus);
    return () => { clearTimeout(timer); clearInterval(fallback); window.removeEventListener('focus', onFocus); void client.deactivate(); };
  }, [enabled]);
}
