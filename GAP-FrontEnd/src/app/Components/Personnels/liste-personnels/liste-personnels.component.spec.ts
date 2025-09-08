import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListePersonnelsComponent } from './liste-personnels.component';

describe('ListePersonnelsComponent', () => {
  let component: ListePersonnelsComponent;
  let fixture: ComponentFixture<ListePersonnelsComponent>;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ListePersonnelsComponent]
    });
    fixture = TestBed.createComponent(ListePersonnelsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
