import{createContext,useContext,useState}from"react";
const C=createContext();
export function GameProvider({children}){const[session,setSession]=useState(()=>JSON.parse(localStorage.getItem("skribblSession")||"null"));function save(v){setSession(v);localStorage.setItem("skribblSession",JSON.stringify(v));}return <C.Provider value={{session,save}}>{children}</C.Provider>}
export const useGame=()=>useContext(C);
