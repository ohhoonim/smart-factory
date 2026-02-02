import { createFileRoute } from "@tanstack/react-router";
import { useState } from "react";

export const Route = createFileRoute("/practice/basic/form")({
  component: RouteComponent,
});

interface Search {
  username: string;
  age: number;
  gender: "male" | "female";
  graduate?: string[];
}

function RouteComponent() {
  const [search, setSearch] = useState<Search>({
    username: "",
    age: 0,
    gender: "male",
    graduate: [],
  });

  type El = HTMLInputElement | HTMLSelectElement | HTMLTextAreaElement;

  const handleForm = (e: React.ChangeEvent<El>): void => {
    const { name, value, type } = e.target;

    const isCheckboxOrRadio =
      e.target instanceof HTMLInputElement &&
      (e.target.type === "checkbox" || e.target.type === "radio");

    const checked = isCheckboxOrRadio
      ? (e.target as HTMLInputElement).checked
      : false;

    if (name === "graduate" && type === "checkbox") {
      setSearch((prevSearch) => {
        const prevGraduate = prevSearch.graduate || [];

        if (checked) {
          if (!prevGraduate.includes(value)) {
            return {
              ...prevSearch,
              graduate: [...prevGraduate, value],
            };
          }
        } else {
          return {
            ...prevSearch,
            graduate: prevGraduate.filter((item) => item !== value),
          };
        }
        return prevSearch;
      });
    } else {
      setSearch({
        ...search,
        [name as keyof Search]: value,
      });
    }
  };

  return (
    <>
      <label>
        아이디 :{" "}
        <input
          type="text"
          name="username"
          value={search.username}
          onChange={handleForm}
        />
      </label>
      <label>
        성별 : 남{" "}
        <input
          type="radio"
          name="gender"
          value="male"
          checked={search.gender === "male"}
          onChange={handleForm}
        />
        여{" "}
        <input
          type="radio"
          name="gender"
          value="female"
          checked={search.gender === "female"}
          onChange={handleForm}
        />
      </label>
      <div>
        초 :{" "}
        <input
          type="checkbox"
          name="graduate"
          value="element"
          onChange={handleForm}
          checked={search.graduate?.includes("element")}
        />
        중 :{" "}
        <input
          type="checkbox"
          name="graduate"
          value="middle"
          onChange={handleForm}
          checked={search.graduate?.includes("middle")}
        />
        고 :{" "}
        <input
          type="checkbox"
          name="graduate"
          value="high"
          onChange={handleForm}
          checked={search.graduate?.includes("high")}
        />
      </div>
      <p>{JSON.stringify(search)}</p>
    </>
  );
}
